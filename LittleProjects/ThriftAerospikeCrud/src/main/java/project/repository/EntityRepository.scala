package project.repository

import java.util.Calendar

import cats.effect.Sync
import com.typesafe.config.Config
import com.aerospike.client.{AerospikeClient, Bin, Key, Record}
import com.aerospike.client.policy._
import cats.implicits._
import crud.Entity
import org.apache.http.MethodNotSupportedException
import org.apache.logging.log4j.{LogManager, Logger}

class EntityRepository[F[_]: Sync](config: Config) extends Repository[F, Entity, Long] {
  val logger: Logger = LogManager.getLogger(classOf[EntityRepository[F]])

  private val client: AerospikeClient = {
    val policy = new ClientPolicy

    policy.readPolicyDefault.consistencyLevel = ConsistencyLevel.CONSISTENCY_ALL
    policy.readPolicyDefault.maxRetries = 3

    policy.writePolicyDefault.commitLevel = CommitLevel.COMMIT_ALL
    policy.writePolicyDefault.expiration = 120

    new AerospikeClient(policy, config.getString("host"), config.getInt("port"))
  }

  private val namespace = config.getString("namespace")
  private val setName = config.getString("set.name")

  override def findOne(id: Long): F[Option[Entity]] = for {
    _ <- Sync[F].pure[Unit](logger.info(s"Find one by id: $id"))
   r <- Sync[F].pure[Option[Entity]](Option(client.get(null, new Key(namespace, setName, id))))
    _ <- Sync[F].pure[Unit](logger.info(s"Result: $r"))
  } yield r

  override def findAll(): F[Seq[Entity]] =
    throw new MethodNotSupportedException("findAll is not supported")

  override def save(a: Entity): F[Entity] = {
    val writerPolicy = new WritePolicy()
    writerPolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY

    for {
      _ <- Sync[F].pure[Unit](client.add(writerPolicy, new Key(namespace, setName, a.id), entityToBins(a): _*))
      entity <- Sync[F].pure[Entity](a)
    } yield entity
  }

  override def update(a: Entity): F[Unit] = {
    val writerPolicy = new WritePolicy()
    writerPolicy.recordExistsAction = RecordExistsAction.REPLACE_ONLY

    for {
      _ <- Sync[F].pure[Unit](client.add(writerPolicy, new Key(namespace, setName, a.id), entityToBins(a): _*))
      entity <- Sync[F].pure[Entity](a)
    } yield entity
  }

  override def remove(id: Long): F[Boolean] = for {
    r <- Sync[F].pure[Boolean](client.delete(null, new Key(namespace, setName, id)))
  } yield r

  override def removeAll(): F[Unit] = for {
    r <- Sync[F].pure[Unit](client.truncate(null, namespace, setName, Calendar.getInstance()))
  } yield r

  private implicit def recordToEntity(record: Record): Entity = if (record != null) {
    val entity = new Entity()
    entity.id = record.getLong("id")
    entity.value = record.getString("value")
    entity.timestamp = record.getLong("timestamp")

    entity
  } else {
    null
  }

  private implicit def entityToBins(entity: Entity): Seq[Bin] = Seq(
    new Bin("id", entity.id),
    new Bin("value", entity.value),
    new Bin("timestamp", entity.timestamp)
  )
}

object EntityRepository {
  def apply[F[_]: Sync](config: Config): EntityRepository[F] =
    new EntityRepository[F](config)
}
