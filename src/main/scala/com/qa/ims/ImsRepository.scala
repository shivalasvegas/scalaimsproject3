package com.qa.ims

import reactivemongo.api.bson.collection.BSONCollection
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{AsyncDriver, Cursor, DB, MongoConnection}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONString, Macros, document}

object ImsRepository {

  val mongoUri = "mongodb://localhost:27017"

  import ExecutionContext.Implicits.global

  // Connect to the database
  val driver = AsyncDriver()
  val parsedUri = MongoConnection.fromString(mongoUri)

  // Database and collections: Get references
  val futureConnection = parsedUri.flatMap(driver.connect(_))
  def db1: Future[DB] = futureConnection.flatMap(_.database("scalaimstestdb"))

  def customerCollection: Future[BSONCollection] = db1.map(_.collection("customer"))
  def itemCollection: Future[BSONCollection] = db1.map(_.collection("item"))
  def orderCollection: Future[BSONCollection] = db1.map(_.collection("order"))

  implicit def customerWriter: BSONDocumentWriter[Customer] = Macros.writer[Customer]
  implicit def ItemWriter: BSONDocumentWriter[Item] = Macros.writer[Item]
  implicit def OrderWriter: BSONDocumentWriter[Order] = Macros.writer[Order]
  implicit def databaseWriter: BSONDocumentWriter[DatabaseUser] = Macros.writer[DatabaseUser]
  implicit def customerReader: BSONDocumentReader[CustomerCase] = Macros.reader[CustomerCase]

//  def createCustomer(customer: CustomerCase): Future[Unit] =
//    customerCollection.flatMap(_.insert.one(customer).map(_ => {}))

  def create(databaseUser: DatabaseUser): Future[Unit] = {
    databaseUser match {
      case a: Customer => customerCollection.flatMap(_.insert.one(databaseUser).map(_ => {}))
      case b: Item => itemCollection.flatMap(_.insert.one(databaseUser).map(_ => {}))
      case c: Order => orderCollection.flatMap(_.insert.one(databaseUser).map(_ => {}))
    }

  }

  // specific customer queries
  def findCustomerByName(forename: String): Future[List[CustomerCase]] =
    customerCollection.flatMap(_.find(BSONDocument("forename" -> forename)).
      cursor[CustomerCase]().
      collect[List](-1, Cursor.FailOnError[List[CustomerCase]]()))

  def findCustomerById(customer: CustomerCase): Future[List[CustomerCase]] =
    customerCollection.flatMap(_.find(BSONDocument("_id" -> customer._id)). // query builder
      cursor[CustomerCase](). // using the result cursor
      collect[List](-1, Cursor.FailOnError[List[CustomerCase]]()))

    def readAll(): Unit = {
        val customers: Future[List[CustomerCase]] = customerCollection.flatMap(_.find(document())
          .cursor[CustomerCase]()
          .collect[List](-1, Cursor.FailOnError[List[CustomerCase]]()))
        customers andThen {
          case Success(value) => {
            value.foreach(customer => println(customer.toString))
          }
          case Failure(e) => {
            println(e.getMessage.toString)
          }
        }
    }


}
