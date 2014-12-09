package at.ac.tuwien.dsg.salam.rest.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import at.ac.tuwien.dsg.smartcom.adapters.RESTInputAdapter;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.salam.cloud.discoverer.Discoverer;
import at.ac.tuwien.dsg.salam.cloud.scheduler.DependencyProcessor;
import at.ac.tuwien.dsg.salam.common.interfaces.ComposerInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.DependencyProcessorInterface;
import at.ac.tuwien.dsg.salam.common.interfaces.DiscovererInterface;
import at.ac.tuwien.dsg.salam.common.model.Assignment;
import at.ac.tuwien.dsg.salam.composer.Composer;
import at.ac.tuwien.dsg.salam.rest.common.Util;
import at.ac.tuwien.dsg.salam.rest.config.AppConfig;
import at.ac.tuwien.dsg.salam.rest.exceptions.CollectiveNotAvailable;
import at.ac.tuwien.dsg.salam.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.salam.rest.resource.Collective;
import at.ac.tuwien.dsg.salam.rest.resource.Peer;
import at.ac.tuwien.dsg.salam.rest.resource.Task;
import at.ac.tuwien.dsg.salam.rest.resource.Task.SeverityLevel;
import at.ac.tuwien.dsg.salam.util.ConfigJson;
import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.SmartCom;
import at.ac.tuwien.dsg.smartcom.adapters.EmailInputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;

@Service
public class TaskService {

    private static ConcurrentMap<Integer, Task> tasks = new ConcurrentHashMap<Integer, Task>(); 
    private static TaskGenerator generator;
    private static PeerService peerManager;
    private static ComposerInterface composer;
    private static Communication communication;

    public TaskService() {
        try {

            String config = System.getProperty(AppConfig.SALAM_REST_CONFIG);
            
            String taskGeneratorConfigFile = Util.getProperty(config, "task_generator_rule");
            String composerConfigFile = Util.getProperty(config, "composer_config");

            // init task generator
            ConfigJson taskGeneratorRuleConfig = new ConfigJson(taskGeneratorConfigFile);
            generator = new TaskGenerator(taskGeneratorRuleConfig);

            // init components
            peerManager = new PeerService();
            DiscovererInterface discoverer = new Discoverer(peerManager);
            DependencyProcessorInterface dp = new DependencyProcessor();
            composer = new Composer(composerConfigFile, peerManager, discoverer, dp);

            // init smartcom
            SmartCom smartCom = new SmartCom(peerManager, peerManager, peerManager);
            smartCom.setRestApiPort(9090);
            smartCom.initializeSmartCom();
            communication = smartCom.getCommunication();
            communication.registerNotificationCallback(new NotificationHandler());

            EmailInputAdapter input = new EmailInputAdapter("Task",
                    PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
                    PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
                    PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
                    Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
                    true, "TASK", "", true);
            //communication.addPullAdapter(input, 5000);

            RESTInputAdapter adapter = new RESTInputAdapter(9091, "response");
            communication.addPushAdapter(adapter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    public TaskGenerator getGenerator() {
        return generator;
    }


    public ComposerInterface getComposer() {
        return composer;
    }
    
    public List<Task> getTasks(int page, int pageSize) {
        final List<Task> slice = new ArrayList<Task>( pageSize );

        final Iterator<Task> iterator = tasks.values().iterator();
        for( int i = 0; slice.size() < pageSize && iterator.hasNext(); ) {
            if( ++i > ( ( page - 1 ) * pageSize ) ) {
                slice.add(iterator.next());
            }
        }

        return slice;
    }

    public Task getById(Integer id) {
        final Task task = tasks.get(id);

        if(task == null) {
            throw new NotFoundException();
        }

        return task;
    }

    public Task createTask(String name, String content, String tag, SeverityLevel severity) {

        // create task
        final Task task = new Task(0, name, content, tag, severity);

        // create mapped salam task
        at.ac.tuwien.dsg.salam.common.model.Task salamTask;
        salamTask = generator.createSalamTask(task);

        // can't find rule
        if (salamTask==null) {
            throw new NotFoundException();
        }
        System.out.println("Task created: " + salamTask.detail());
        
        // set task id
        task.setId(salamTask.getId());

        // handle task
        handleTask(task, salamTask);
        
        // put into the cache
        tasks.put(salamTask.getId(), task);

        return task;
    }

    public void removeTask(Integer id) {
        if( tasks.remove(id) == null ) {
            throw new NotFoundException();
        }
    }

    public List<Assignment> handleTask(Task task,
            at.ac.tuwien.dsg.salam.common.model.Task salamTask) {

        List<Assignment> assignments = null;
        
        // compose collective
        assignments = composer.compose(salamTask, 0);
        
        if (assignments==null || assignments.size()==0) {
            throw new CollectiveNotAvailable();
        }

        // register
        Identifier collectiveIdentifier = peerManager.registerCollective(task, assignments, DeliveryPolicy.Collective.TO_ALL_MEMBERS);
        Integer collectiveId = Integer.parseInt(collectiveIdentifier.getId());
        task.setCollectiveId(collectiveId);
        
        System.out.println("=== Collective created for task " + salamTask + " ===");
        for (Assignment assignment: assignments) {
            System.out.println(assignment.getAssignee());
            // TODO: shoud do it using a hook to composer
            assignment.getAssignee().getProvider().addAssignmentCount();
        }
        System.out.println("=====================================");

        // send message
        sendMessageToAssignedPeers(collectiveId, task, assignments);

        return assignments;
    }

    public void handleDelegation(Collective collective, at.ac.tuwien.dsg.salam.rest.resource.Assignment assignment) {
        
        List<String> includedServices = new ArrayList<String>(1);
        includedServices.add(assignment.getService());

        // create new salam task for composition
        at.ac.tuwien.dsg.salam.common.model.Task salamTask = generator.createSalamTask(collective.getTask(), includedServices);
        
        // compose
        List<Assignment> assignments = null;
        assignments = composer.compose(salamTask, 0);
        
        System.out.println("=== Collective created for task delegation " + salamTask + " ===");
        for (Assignment a: assignments) {
            // add to collective
            Peer peer = peerManager.getPeerById((int)a.getAssignee().getProvider().getId());
            collective.addAssignment(a.getRole().getFunctionality().getName(), peer);
            System.out.println(a.getAssignee());
            // TODO: shoud do it using a hook to composer
            a.getAssignee().getProvider().addAssignmentCount();
        }
        System.out.println("=====================================");

        // send message
        sendMessageToAssignedPeers(collective.getId(), collective.getTask(), assignments);
    }
    
    private void sendMessageToAssignedPeers(Integer collectiveId, Task task, List<Assignment> assignments) {
        try {
            // build message
            String content = 
                    "You got a task:<br>\n" +
                    "Task #" + task.getId() + "<br>\n" +
                    "Tag: " + task.getTag() + "<br>\n" +
                    "Severity: " + task.getSeverity() + "<br>\n" +
                    task.getContent() + "<br><br>\n";
    
            for (Assignment assignment: assignments) {
                int elementId = (int)assignment.getAssignee().getProvider().getId();
                Peer peer = peerManager.getPeerById(elementId);
                Identifier peerId = Identifier.peer(Integer.toString(elementId));
                String message = content + 
                        generateTaskActionLink(collectiveId, elementId, "See details");
                Message.MessageBuilder builder = new Message.MessageBuilder()
                    .setType("TASK")
                    .setSubtype("")
                    .setReceiverId(peerId)
                    .setContentType("text/html")
                    .setSenderId(Identifier.component("DEMO"))
                    .setConversationId(System.nanoTime() + "")
                    .setContent(message);
                Message msg = builder.create();
                System.out.println("Sending message to " + peer.getEmail() + ":\n" + message);
                // create input adapter using email
//                EmailInputAdapter input = new EmailInputAdapter(msg.getConversationId(),
//                        PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
//                        PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
//                        PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
//                        Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
//                        true, "test", "test", true);
//                communication.addPullAdapter(input, 1000, false);

                conversationToCollectiveMapping.put(msg.getConversationId(), collectiveId);

                // send
                communication.send(msg);
            }
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> conversationToCollectiveMapping = new ConcurrentHashMap<>();
    
    private String generateTaskActionLink(Integer collectiveId, Integer peerId, String title) {
        String link = "";
        String host = System.getProperty(AppConfig.SERVER_HOST);
        String port = System.getProperty(AppConfig.SERVER_PORT);
        String path = System.getProperty(AppConfig.WEBUI_CONTEXT_PATH);
        link = "<a href='http://" + host + ":" + port + "/" + path + "/assignment.html#/status/" + collectiveId + "/" + peerId + "'>" + title + "</a>";
        return link;
    }

    private class NotificationHandler implements NotificationCallback {

        public void notify(Message message) {
            if (PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType())) {
                return;
            }

            if ("TASK".equals(message.getType())) {
                try {
                    at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status status;

                    switch (message.getSubtype().toUpperCase()) {
                        case "ACCEPT":
                            status = at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.RUNNING;
                            break;
                        case "DELEGATE":
                            status = at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.DELEGATED;
                            break;
                        case "TERMINATE":
                            status = at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.TERMINATED;
                            break;
                        case "FINISH":
                            status = at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.FINISHED;
                            break;
                        default:
                            status = at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.valueOf(message.getSubtype());
                    }

                    Collective collective = peerManager.getCollectiveById(conversationToCollectiveMapping.get(message.getConversationId()));
                    for (at.ac.tuwien.dsg.salam.rest.resource.Assignment assignment: collective.getAssignments()) {
                        if (assignment.getPeer().getElementId().equals(Long.valueOf(message.getSenderId().getId()))) {
                            assignment.setStatus(status);
                            if (status== at.ac.tuwien.dsg.salam.rest.resource.Assignment.Status.DELEGATED) {
                                handleDelegation(collective, assignment);
                            }
                            break;
                        }
                    }
                } catch(Exception e) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("################ New Message received ################").append("\n");
                    builder.append(message.toString()).append("\n");
                    builder.append("######################################################");
                    System.out.println(builder.toString());
                }
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("################ New Message received ################").append("\n");
                builder.append(message.toString()).append("\n");
                builder.append("######################################################");
                System.out.println(builder.toString());
            }
        }
    }
    
}