如图所示：Application 通过 submit 被提交到机器上后，该节点会启动一个 Driver 进程。

1. Driver 来开始执行 Application 应用程序，首先会初始化 SparkContext，实例化SparkContext；
2. SparkContext 实例化后，就会构建 DAGScheduler 和 TaskScheduler；
3. TaskScheduler 会通过对应的后台进程去连接 master，向 Mater 注册 Application 应用；
4. Master 收到 Application 注册请求后，会通过资源调度算法，在 worker 节点上为这个 Application 应用启动多个 Executor；
5. Executor 启动之后，会向 TaskScheduler 反向注册上去；
6. 当所有 Executor 都反向注册到 Driver 后，Driver 会结束初始化 SparkContext；

上述流程基本上就完成了资源的分配，接下来就开始实际执行 Application 中的任务了。

7. 当应用程序执行到 action 时，就会创建一个 job，并将 job 提交给 DAGScheduler；
	job 是 Application 应用程序所有任务的集合
8.  DAGScheduler 的作用就是：将 job 划分为多个 Stage，并为每个 stage 创建 TaskSet；
9. TaskScheduler 会将 TaskSet 中的 Task 提交到 Executor 上去执行；
10. Executor 接收到 task后，会使用 TaskRunner 来封装 Task，然后从线程池中取出一个线程来执行这个 Task；
11. 每个 Task 针对一个 Partition来执行 Application程序的算子和函数，直到所有操作执行完成。

这里需要注意的是：

DAGScheduler 的 stage 划分算法是通过反向划分的方式来处理，从 action开始作为 final stage，也就是 ResultTask，逆向反推，如果是窄依赖，那么就可以将其划分为一个 stage 内，如果遇到 shuffle 依赖，则将作为第二个 stage 的开始，继续逆向推导。遇到 shuffle 依赖就会生成一个新的 stage，遇到窄依赖就将其划到当前的 stage 中。其中最后一个 stage 称为 ResultTask，其他 stage 称为 ShuffleMapTask。

![在这里插入图片描述](pictures/Spark内核架构.png)


Master： 集群的领导者，负责管理集群资源，接收 Client 提交的作业，以及向 Worker 发送命令。

Worker： 执行 Master 发送的指令，来具体分配资源，并在这些资源中执行任务。

Driver：一个 Spark 作业运行时会启动一个 Driver 进程，也是作业的主进程，负责作业的解析、生成 Stage，并调度 Task 到 Executor 上。

Executor：真正执行作业的地方。Executor 分布在集群的 Worker 上，每个 Executor 接收 Driver 的命令来加载和运行 Task，一个 Executor 可以执行一个或多个 Task。

SparkContext：程序运行调度的核心，由高层调度器 DAGSchedule 划分程序的每个阶段，底层调度器 TaskScheduler 划分每个阶段的具体任务。 SchedulerBackend 管理整个集群中为正在运行的程序分配的计算资源 Executor。
	-- DAFScheduler：负责高层调度，划分 stage 并生成程序运行的有向无环图。
	-- TakScheduler：负责具体 stage 内部的底层调度，具体 task 的调度、容错等。

Job：是 Top_level 的工作单元。正在执行的叫 ActiveJob。每个 Action 算子都会触发一次 Job，一个 Job 可能包含一个或多个 Stage.

Stage：是用来计算中间结果的 TaskSets。TaskSets 中的 Task 逻辑对于同一个 RDD 内的不同 partition 都一样。 Stage 在 Shuffle 的地方产生，此时下一次 Stage 要用到上一个 Stage 的全部数据，所以要等到上一个 Stage 全部执行完才能开始。Stage 有两种：ShuffleMapStage 和 ResultStage， 除了最后一个 Stage 是 ResultStage 外，其他 Stage 都是 ShuffleMapStage。 ShuffleMapStage 会产生中间结果，以文件的方式保存在集群里， Stage 经常被不同的 Job 共享，前提是这些 Job 重用了同一个 RDD。

RDD： 是不可变、Lazy级别的、粗粒度的数据集合，包含了一个或多个数据分片，即 partition。

Task：任务执行的工作单元，每个 Task 会被发送到一个节点上，每个 Task 对应 RDD 的一个 Partition。

![image-20191108075711439](pictures/系统调度流程.png)