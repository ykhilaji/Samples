package core.configuration.listeners
import com.datastax.driver.core._
import org.slf4j.{Logger, LoggerFactory}

class SchemaChangeListener extends com.datastax.driver.core.SchemaChangeListener {
  private val logger: Logger = LoggerFactory.getLogger("SchemaChangeListener")

  override def onKeyspaceChanged(current: KeyspaceMetadata, previous: KeyspaceMetadata): Unit = {}

  override def onTableChanged(current: TableMetadata, previous: TableMetadata): Unit = {}

  override def onUnregister(cluster: Cluster): Unit = {}

  override def onMaterializedViewAdded(view: MaterializedViewMetadata): Unit = {}

  override def onMaterializedViewRemoved(view: MaterializedViewMetadata): Unit = {}

  override def onTableAdded(table: TableMetadata): Unit = logger.info(s"Table added: ${table.getName}")

  override def onRegister(cluster: Cluster): Unit = {}

  override def onAggregateRemoved(aggregate: AggregateMetadata): Unit = {}

  override def onUserTypeAdded(`type`: UserType): Unit = logger.info(s"User type added: ${`type`.getName}")

  override def onUserTypeChanged(current: UserType, previous: UserType): Unit = {}

  override def onAggregateChanged(current: AggregateMetadata, previous: AggregateMetadata): Unit = {}

  override def onMaterializedViewChanged(current: MaterializedViewMetadata, previous: MaterializedViewMetadata): Unit = {}

  override def onFunctionAdded(function: FunctionMetadata): Unit = {}

  override def onFunctionRemoved(function: FunctionMetadata): Unit = {}

  override def onAggregateAdded(aggregate: AggregateMetadata): Unit = {}

  override def onUserTypeRemoved(`type`: UserType): Unit = {}

  override def onKeyspaceRemoved(keyspace: KeyspaceMetadata): Unit = {}

  override def onTableRemoved(table: TableMetadata): Unit = {}

  override def onKeyspaceAdded(keyspace: KeyspaceMetadata): Unit = logger.info(s"Keyspace added: ${keyspace.getName}")

  override def onFunctionChanged(current: FunctionMetadata, previous: FunctionMetadata): Unit = {}
}
