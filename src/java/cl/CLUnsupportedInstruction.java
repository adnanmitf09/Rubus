/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

package cl;

import graph.instructions.Instruction;

/**
 * Exception that can be thrown by exporters if the instruction can not be
 * performed in the given output. This can also be used to indicate that the
 * instruction export has not been implemented.
 */
public class CLUnsupportedInstruction extends RuntimeException {
  /**
   * Instruction concerned.
   */
  private Instruction instruction;

  /**
   * Export target.
   */
  private String target;

  /**
   * Reason for failure (optional).
   */
  private String reason;

  /**
   * Constructs the exception in the case where all details are given.
   *
   * @param ins    Unsupported instruction.
   * @param target Target concerned.
   * @param reason Full reason that export failed.
   */
  public CLUnsupportedInstruction(Instruction ins, String target, String reason) {
    this.instruction = ins;
    this.target      = target;
    this.reason      = reason;
  }

  /**
   * Constructs the exception in the case without a full reason.
   *
   * @param ins    Unsupported instruction.
   * @param target Target concerned.
   */
  public CLUnsupportedInstruction(Instruction ins, String target) {
    this(ins, target, "");
  }

  /**
   * Returns the actual instruction node that caused the exception.
   *
   * @return       Instruction.
   */
  public Instruction getSpecificInstruction() {
    return instruction;
  }

  /**
   * Returns the name of the instruction that caused the exception.
   * 
   * @return       Instruction name.
   */
  public String getInstruction() {
    return instruction.getClass().getSimpleName();
  }

  /**
   * Returns the target that threw the exception.
   *
   * @return       Export target.
   */
  public String getTarget() {
    return target;
  }

  /**
   * Returns the full reason for the failure.
   *
   * @return       Full reason.
   */
  public String getReason() {
    return reason;
  }

  /**
   * Full description of exception.
   *
   * @return       Description.
   */
  @Override
  public String toString() {
    return "Could not export '" + getInstruction() + "' to '" + getTarget()
         + "': " + getReason();
  }
}
