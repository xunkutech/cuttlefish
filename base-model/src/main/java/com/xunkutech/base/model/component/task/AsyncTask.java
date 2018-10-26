package com.xunkutech.base.model.component.task;

import com.xunkutech.base.model.AbstractModelBean;
import com.xunkutech.base.model.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by jason on 7/15/17.
 */
@Getter
@Setter
@Embeddable
//@Table(indexes = {@Index(columnList = "queue_name, sequence", unique = true)})
public class AsyncTask<P> extends AbstractModelBean<P> {


    private static final long serialVersionUID = 5392747285559229116L;

    @Column(name = "batch_code",
            length = 64,
            columnDefinition = "CHAR(64) COLLATE 'ascii_bin'")
    private String batchCode;

    /**
     * task chain
     */
    @Column(name = "task_chain",
            length = 4092,
            columnDefinition = "VARCHAR(4092) COLLATE 'ascii_bin'")
    private String taskChain;

    /**
     * 队列状态
     */
    @Column(name = "task_status",
            nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0")
    private TaskStatus taskStatus;

    /**
     * 队列的自增序列号
     */
    @Column(name = "sequence",
            nullable = false,
            insertable = false,
            updatable = false)
    private Long sequence;

    /**
     * 回滚标记位。如果此标记为为true。则当前未提交的事务回滚。
     * 当前已提交的事务做业务逆操作。
     * 此标记位初始化的时候为false。当当前任务集中首次出现failed任务时。
     * 任务集中的所有task的task_rollback都置为true;
     */
    @Column(
            name = "task_rollback",
            columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean taskRollback = false;

    /**
     * The current worker thread that acclaimed to process the task. This step forces
     * to convert thread unsafe to thread safe.
     */
    @Column(name = "worker_name",
            length = 500,
            columnDefinition = "VARCHAR(510) COLLATE 'ascii_bin'")
    private String workerName;

    /**
     * if the task's status is DISPATCHED.(including sub-tasks) executed successfully.
     */
    @Column(name = "succeed_bit",
            length = 500,
            columnDefinition = "VARCHAR(510) COLLATE 'ascii_bin'")
    private Long succeedFlag;

    private Long failedFlag;

    private Long undoFlag;
}
