/**
 * Copyright (C) 2018 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.actor.typed.scaladsl

import java.util.function.Consumer
import java.util.function.{ Function ⇒ JFunction }

import scala.annotation.tailrec

import akka.annotation.DoNotInherit
import akka.actor.typed.Behavior
import akka.actor.typed.internal.ImmutableStashBufferImpl
import akka.actor.typed.internal.MutableStashBufferImpl

object ImmutableStashBuffer {
  /**
   * Create an empty message buffer.
   *
   * @param capacity the buffer can hold at most this number of messages
   * @return an empty message buffer
   */
  def apply[T](capacity: Int): ImmutableStashBuffer[T] =
    ImmutableStashBufferImpl[T](capacity)

}

/**
 * A thread safe immutable message buffer that can be used to buffer messages inside actors.
 *
 * The buffer can hold at most the given `capacity` number of messages.
 */
@DoNotInherit trait ImmutableStashBuffer[T] {

  /**
   * Check if the message buffer is empty.
   *
   * @return if the buffer is empty
   */
  def isEmpty: Boolean

  /**
   * Check if the message buffer is not empty.
   *
   * @return if the buffer is not empty
   */
  def nonEmpty: Boolean

  /**
   * How many elements are in the message buffer.
   *
   * @return the number of elements in the message buffer
   */
  def size: Int

  /**
   * @return `true` if no more messages can be added, i.e. size equals the capacity of the stash buffer
   */
  def isFull: Boolean

  /**
   * Add one element to the end of the message buffer. Note that this class is
   * immutable so the returned instance contains the added message.
   *
   * @param message the message to buffer, must not be `null`
   * @return this message buffer
   * @throws  `StashOverflowException` is thrown if the buffer [[MutableStashBuffer#isFull]].
   */
  def stash(message: T): ImmutableStashBuffer[T]

  /**
   * Add one element to the end of the message buffer. Note that this class is
   * immutable so the returned instance contains the added message.
   *
   * @param message the message to buffer, must not be `null`
   * @return this message buffer
   * @throws  `StashOverflowException` is thrown if the buffer [[MutableStashBuffer#isFull]].
   */
  def :+(message: T): ImmutableStashBuffer[T] = stash(message)

  /**
   * Remove the first element of the message buffer. Note that this class is
   * immutable so the head element is removed in the returned instance.
   *
   * @throws `NoSuchElementException` if the buffer is empty
   */
  def dropHead(): ImmutableStashBuffer[T]

  /**
   * Remove the first `numberOfMessages` of the message buffer. Note that this class is
   * immutable so the elements are removed in the returned instance.
   */
  def drop(numberOfMessages: Int): ImmutableStashBuffer[T]

  /**
   * Return the first element of the message buffer.
   *
   * @return the first element or throws `NoSuchElementException` if the buffer is empty
   * @throws `NoSuchElementException` if the buffer is empty
   */
  def head: T

  /**
   * Iterate over all elements of the buffer and apply a function to each element.
   *
   * @param f the function to apply to each element
   */
  def foreach(f: T ⇒ Unit): Unit

  /**
   * Process all stashed messages with the `behavior` and the returned
   * [[Behavior]] from each processed message.
   */
  def unstashAll(ctx: ActorContext[T], behavior: Behavior[T]): Behavior[T]

  /**
   * Process `numberOfMessages` of the stashed messages with the `behavior`
   * and the returned [[Behavior]] from each processed message.
   */
  def unstash(ctx: ActorContext[T], behavior: Behavior[T], numberOfMessages: Int, wrap: T ⇒ T): Behavior[T]

}

object MutableStashBuffer {

  /**
   * Create an empty message buffer.
   *
   * @param capacity the buffer can hold at most this number of messages
   * @return an empty message buffer
   */
  def apply[T](capacity: Int): MutableStashBuffer[T] =
    MutableStashBufferImpl[T](capacity)
}

/**
 * A non thread safe mutable message buffer that can be used to buffer messages inside actors.
 *
 * The buffer can hold at most the given `capacity` number of messages.
 */
@DoNotInherit trait MutableStashBuffer[T] {
  /**
   * Check if the message buffer is empty.
   *
   * @return if the buffer is empty
   */
  def isEmpty: Boolean

  /**
   * Check if the message buffer is not empty.
   *
   * @return if the buffer is not empty
   */
  def nonEmpty: Boolean

  /**
   * How many elements are in the message buffer.
   *
   * @return the number of elements in the message buffer
   */
  def size: Int

  /**
   * @return `true` if no more messages can be added, i.e. size equals the capacity of the stash buffer
   */
  def isFull: Boolean

  /**
   * Add one element to the end of the message buffer.
   *
   * @param message the message to buffer
   * @return this message buffer
   * @throws  `StashOverflowException` is thrown if the buffer [[MutableStashBuffer#isFull]].
   */
  def stash(message: T): MutableStashBuffer[T]

  /**
   * Return the first element of the message buffer and removes it.
   *
   * @return the first element or throws `NoSuchElementException` if the buffer is empty
   * @throws `NoSuchElementException` if the buffer is empty
   */
  def dropHead(): T

  /**
   * Return the first element of the message buffer without removing it.
   *
   * @return the first element or throws `NoSuchElementException` if the buffer is empty
   * @throws `NoSuchElementException` if the buffer is empty
   */
  def head: T

  /**
   * Iterate over all elements of the buffer and apply a function to each element.
   *
   * @param f the function to apply to each element
   */
  def foreach(f: T ⇒ Unit): Unit

  /**
   * Process all stashed messages with the `behavior` and the returned
   * [[Behavior]] from each processed message. The `MutableStashBuffer` will be
   * empty after processing all messages, unless an exception is thrown.
   * If an exception is thrown by processing a message a proceeding messages
   * and the message causing the exception have been removed from the
   * `MutableStashBuffer`, but unprocessed messages remain.
   */
  def unstashAll(ctx: ActorContext[T], behavior: Behavior[T]): Behavior[T]

  /**
   * Process `numberOfMessages` of the stashed messages with the `behavior`
   * and the returned [[Behavior]] from each processed message.
   * If an exception is thrown by processing a message a proceeding messages
   * and the message causing the exception have been removed from the
   * `MutableStashBuffer`, but unprocessed messages remain.
   */
  def unstash(ctx: ActorContext[T], behavior: Behavior[T], numberOfMessages: Int, wrap: T ⇒ T): Behavior[T]

}

/**
 * Is thrown when the size of the stash exceeds the capacity of the stash buffer.
 */
class StashOverflowException(message: String) extends RuntimeException(message)
