package at.ac.tuwien.dsg.hcu.rest.resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Task", description = "Task representation")
public class Task {

    @ApiModelProperty(value = "Task's id", required = true)
    private Integer id;

    @ApiModelProperty(value = "The date when the task was submitted", required = true)
    private Calendar timeCreated;

    @ApiModelProperty(value = "Task's name", required = true)
    private String name;

    @ApiModelProperty(value = "Task's content", required = true)
    private String content;

    @ApiModelProperty(value = "Task's tag, e.g., a category", required = true)
    private String tag; 

    @ApiModelProperty(value = "Task's severity", required = true)
    private SeverityLevel severity;

    @ApiModelProperty(value = "The id of the assigned collective", required = true)
    private Integer collectiveId;

    public enum SeverityLevel {
        NOTICE, WARNING, CRITICAL, ALERT, EMERGENCY
    }

    public Task() {
        this.timeCreated = Calendar.getInstance();
    }

    public Task(Integer id, String name, String content,
            String tag, SeverityLevel severity) {
        super();
        this.id = id;
        this.name = name;
        this.content = content;
        this.tag = tag;
        this.severity = severity;
        this.timeCreated = Calendar.getInstance();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public Integer getCollectiveId() {
        return collectiveId;
    }

    public void setCollectiveId(Integer collectiveId) {
        this.collectiveId = collectiveId;
    }

    public String getTimeCreated() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
        return sdf.format(timeCreated.getTime());
    }

    public void setTimeCreated(Calendar timeCreated) {
        this.timeCreated = timeCreated;
    }

}
