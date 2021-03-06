package example

import java.util.UUID
import scalaz.{\/,-\/,\/-,Kleisli}
import scalaz.concurrent.Task
import scalaz.syntax.kleisli._

object Item {
  type Id = UUID
}
case class Item(
  id: Item.Id,
  content: String,
  createdAt: Long)

class ToDo {
  type TConfig = ToDoConfig[Task]
  type ToDoK[A] = Kleisli[Task, TConfig, A]

  protected def config: ToDoK[TConfig] =
    Kleisli.ask[Task, TConfig]

  protected val currentTimeMillis: ToDoK[Long] =
    Task.delay(System.currentTimeMillis).liftKleisli

  protected val randomUUID: ToDoK[UUID] =
    Task.delay(UUID.randomUUID).liftKleisli

  def create(content: String): ToDoK[Item.Id] = {
     for {
      a <- config
      u <- randomUUID
      t <- currentTimeMillis
      i  = Item(u, content, t)
      b <- a.repository.create(i).liftKleisli
    } yield b
  }

  def list: ToDoK[List[Item]] =
    for {
      a <- config
      b <- a.repository.list.liftKleisli
    } yield b
}

