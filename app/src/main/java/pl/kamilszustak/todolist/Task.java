package pl.kamilszustak.todolist;

import java.time.LocalDateTime;
import java.util.Date;

public class Task {

    private State state;
    private String description;
    private Date createDate;
    private Date completeDate;

    public Task(State state, String description, Date createTime) {
        this.state = state;
        this.description = description;
        this.createDate = createTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public Date getCompleteDate() {
        return completeDate;
    }
}
