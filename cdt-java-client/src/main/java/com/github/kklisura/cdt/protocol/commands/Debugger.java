package com.github.kklisura.cdt.protocol.commands;

/*-
 * #%L
 * cdt-java-client
 * %%
 * Copyright (C) 2018 Kenan Klisura
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.kklisura.cdt.protocol.events.debugger.BreakpointResolved;
import com.github.kklisura.cdt.protocol.events.debugger.Paused;
import com.github.kklisura.cdt.protocol.events.debugger.Resumed;
import com.github.kklisura.cdt.protocol.events.debugger.ScriptFailedToParse;
import com.github.kklisura.cdt.protocol.events.debugger.ScriptParsed;
import com.github.kklisura.cdt.protocol.support.annotations.EventName;
import com.github.kklisura.cdt.protocol.support.annotations.Experimental;
import com.github.kklisura.cdt.protocol.support.annotations.Optional;
import com.github.kklisura.cdt.protocol.support.annotations.ParamName;
import com.github.kklisura.cdt.protocol.support.annotations.Returns;
import com.github.kklisura.cdt.protocol.support.types.EventHandler;
import com.github.kklisura.cdt.protocol.support.types.EventListener;
import com.github.kklisura.cdt.protocol.types.debugger.BreakLocation;
import com.github.kklisura.cdt.protocol.types.debugger.EvaluateOnCallFrame;
import com.github.kklisura.cdt.protocol.types.debugger.Location;
import com.github.kklisura.cdt.protocol.types.debugger.RestartFrame;
import com.github.kklisura.cdt.protocol.types.debugger.ScriptPosition;
import com.github.kklisura.cdt.protocol.types.debugger.SearchMatch;
import com.github.kklisura.cdt.protocol.types.debugger.SetBreakpoint;
import com.github.kklisura.cdt.protocol.types.debugger.SetBreakpointByUrl;
import com.github.kklisura.cdt.protocol.types.debugger.SetScriptSource;
import com.github.kklisura.cdt.protocol.types.debugger.State;
import com.github.kklisura.cdt.protocol.types.debugger.TargetCallFrames;
import com.github.kklisura.cdt.protocol.types.runtime.CallArgument;
import java.util.List;

/**
 * Debugger domain exposes JavaScript debugging capabilities. It allows setting and removing
 * breakpoints, stepping through execution, exploring stack traces, etc.
 */
public interface Debugger {

  /**
   * Enables debugger for the given page. Clients should not assume that the debugging has been
   * enabled until the result for this command is received.
   */
  void enable();

  /** Disables debugger for given page. */
  void disable();

  /**
   * Activates / deactivates all breakpoints on the page.
   *
   * @param active New value for breakpoints active state.
   */
  void setBreakpointsActive(@ParamName("active") Boolean active);

  /**
   * Makes page not interrupt on any pauses (breakpoint, exception, dom exception etc).
   *
   * @param skip New value for skip pauses state.
   */
  void setSkipAllPauses(@ParamName("skip") Boolean skip);

  /**
   * Sets JavaScript breakpoint at given location specified either by URL or URL regex. Once this
   * command is issued, all existing parsed scripts will have breakpoints resolved and returned in
   * <code>locations</code> property. Further matching script parsing will result in subsequent
   * <code>breakpointResolved</code> events issued. This logical breakpoint will survive page
   * reloads.
   *
   * @param lineNumber Line number to set breakpoint at.
   */
  SetBreakpointByUrl setBreakpointByUrl(@ParamName("lineNumber") Integer lineNumber);

  /**
   * Sets JavaScript breakpoint at given location specified either by URL or URL regex. Once this
   * command is issued, all existing parsed scripts will have breakpoints resolved and returned in
   * <code>locations</code> property. Further matching script parsing will result in subsequent
   * <code>breakpointResolved</code> events issued. This logical breakpoint will survive page
   * reloads.
   *
   * @param lineNumber Line number to set breakpoint at.
   * @param url URL of the resources to set breakpoint on.
   * @param urlRegex Regex pattern for the URLs of the resources to set breakpoints on. Either
   *     <code>url</code> or <code>urlRegex</code> must be specified.
   * @param columnNumber Offset in the line to set breakpoint at.
   * @param condition Expression to use as a breakpoint condition. When specified, debugger will
   *     only stop on the breakpoint if this expression evaluates to true.
   */
  SetBreakpointByUrl setBreakpointByUrl(
      @ParamName("lineNumber") Integer lineNumber,
      @Optional @ParamName("url") String url,
      @Optional @ParamName("urlRegex") String urlRegex,
      @Optional @ParamName("columnNumber") Integer columnNumber,
      @Optional @ParamName("condition") String condition);

  /**
   * Sets JavaScript breakpoint at a given location.
   *
   * @param location Location to set breakpoint in.
   */
  SetBreakpoint setBreakpoint(@ParamName("location") Location location);

  /**
   * Sets JavaScript breakpoint at a given location.
   *
   * @param location Location to set breakpoint in.
   * @param condition Expression to use as a breakpoint condition. When specified, debugger will
   *     only stop on the breakpoint if this expression evaluates to true.
   */
  SetBreakpoint setBreakpoint(
      @ParamName("location") Location location, @Optional @ParamName("condition") String condition);

  /**
   * Removes JavaScript breakpoint.
   *
   * @param breakpointId
   */
  void removeBreakpoint(@ParamName("breakpointId") String breakpointId);

  /**
   * Returns possible locations for breakpoint. scriptId in start and end range locations should be
   * the same.
   *
   * @param start Start of range to search possible breakpoint locations in.
   */
  @Experimental
  @Returns("locations")
  List<BreakLocation> getPossibleBreakpoints(@ParamName("start") Location start);

  /**
   * Returns possible locations for breakpoint. scriptId in start and end range locations should be
   * the same.
   *
   * @param start Start of range to search possible breakpoint locations in.
   * @param end End of range to search possible breakpoint locations in (excluding). When not
   *     specified, end of scripts is used as end of range.
   * @param restrictToFunction Only consider locations which are in the same (non-nested) function
   *     as start.
   */
  @Experimental
  @Returns("locations")
  List<BreakLocation> getPossibleBreakpoints(
      @ParamName("start") Location start,
      @Optional @ParamName("end") Location end,
      @Optional @ParamName("restrictToFunction") Boolean restrictToFunction);

  /**
   * Continues execution until specific location is reached.
   *
   * @param location Location to continue to.
   */
  void continueToLocation(@ParamName("location") Location location);

  /**
   * Continues execution until specific location is reached.
   *
   * @param location Location to continue to.
   * @param targetCallFrames
   */
  void continueToLocation(
      @ParamName("location") Location location,
      @Experimental @Optional @ParamName("targetCallFrames") TargetCallFrames targetCallFrames);

  /** Steps over the statement. */
  void stepOver();

  /** Steps into the function call. */
  void stepInto();

  /** Steps out of the function call. */
  void stepOut();

  /** Stops on the next JavaScript statement. */
  void pause();

  /**
   * Steps into next scheduled async task if any is scheduled before next pause. Returns success
   * when async task is actually scheduled, returns error if no task were scheduled or another
   * scheduleStepIntoAsync was called.
   */
  @Experimental
  void scheduleStepIntoAsync();

  /** Resumes JavaScript execution. */
  void resume();

  /**
   * Searches for given string in script content.
   *
   * @param scriptId Id of the script to search in.
   * @param query String to search for.
   */
  @Experimental
  @Returns("result")
  List<SearchMatch> searchInContent(
      @ParamName("scriptId") String scriptId, @ParamName("query") String query);

  /**
   * Searches for given string in script content.
   *
   * @param scriptId Id of the script to search in.
   * @param query String to search for.
   * @param caseSensitive If true, search is case sensitive.
   * @param isRegex If true, treats string parameter as regex.
   */
  @Experimental
  @Returns("result")
  List<SearchMatch> searchInContent(
      @ParamName("scriptId") String scriptId,
      @ParamName("query") String query,
      @Optional @ParamName("caseSensitive") Boolean caseSensitive,
      @Optional @ParamName("isRegex") Boolean isRegex);

  /**
   * Edits JavaScript source live.
   *
   * @param scriptId Id of the script to edit.
   * @param scriptSource New content of the script.
   */
  SetScriptSource setScriptSource(
      @ParamName("scriptId") String scriptId, @ParamName("scriptSource") String scriptSource);

  /**
   * Edits JavaScript source live.
   *
   * @param scriptId Id of the script to edit.
   * @param scriptSource New content of the script.
   * @param dryRun If true the change will not actually be applied. Dry run may be used to get
   *     result description without actually modifying the code.
   */
  SetScriptSource setScriptSource(
      @ParamName("scriptId") String scriptId,
      @ParamName("scriptSource") String scriptSource,
      @Optional @ParamName("dryRun") Boolean dryRun);

  /**
   * Restarts particular call frame from the beginning.
   *
   * @param callFrameId Call frame identifier to evaluate on.
   */
  RestartFrame restartFrame(@ParamName("callFrameId") String callFrameId);

  /**
   * Returns source for the script with given id.
   *
   * @param scriptId Id of the script to get source for.
   */
  @Returns("scriptSource")
  String getScriptSource(@ParamName("scriptId") String scriptId);

  /**
   * Defines pause on exceptions state. Can be set to stop on all exceptions, uncaught exceptions or
   * no exceptions. Initial pause on exceptions state is <code>none</code>.
   *
   * @param state Pause on exceptions mode.
   */
  void setPauseOnExceptions(@ParamName("state") State state);

  /**
   * Evaluates expression on a given call frame.
   *
   * @param callFrameId Call frame identifier to evaluate on.
   * @param expression Expression to evaluate.
   */
  EvaluateOnCallFrame evaluateOnCallFrame(
      @ParamName("callFrameId") String callFrameId, @ParamName("expression") String expression);

  /**
   * Evaluates expression on a given call frame.
   *
   * @param callFrameId Call frame identifier to evaluate on.
   * @param expression Expression to evaluate.
   * @param objectGroup String object group name to put result into (allows rapid releasing
   *     resulting object handles using <code>releaseObjectGroup</code>).
   * @param includeCommandLineAPI Specifies whether command line API should be available to the
   *     evaluated expression, defaults to false.
   * @param silent In silent mode exceptions thrown during evaluation are not reported and do not
   *     pause execution. Overrides <code>setPauseOnException</code> state.
   * @param returnByValue Whether the result is expected to be a JSON object that should be sent by
   *     value.
   * @param generatePreview Whether preview should be generated for the result.
   * @param throwOnSideEffect Whether to throw an exception if side effect cannot be ruled out
   *     during evaluation.
   */
  EvaluateOnCallFrame evaluateOnCallFrame(
      @ParamName("callFrameId") String callFrameId,
      @ParamName("expression") String expression,
      @Optional @ParamName("objectGroup") String objectGroup,
      @Optional @ParamName("includeCommandLineAPI") Boolean includeCommandLineAPI,
      @Optional @ParamName("silent") Boolean silent,
      @Optional @ParamName("returnByValue") Boolean returnByValue,
      @Experimental @Optional @ParamName("generatePreview") Boolean generatePreview,
      @Experimental @Optional @ParamName("throwOnSideEffect") Boolean throwOnSideEffect);

  /**
   * Changes value of variable in a callframe. Object-based scopes are not supported and must be
   * mutated manually.
   *
   * @param scopeNumber 0-based number of scope as was listed in scope chain. Only 'local',
   *     'closure' and 'catch' scope types are allowed. Other scopes could be manipulated manually.
   * @param variableName Variable name.
   * @param newValue New variable value.
   * @param callFrameId Id of callframe that holds variable.
   */
  void setVariableValue(
      @ParamName("scopeNumber") Integer scopeNumber,
      @ParamName("variableName") String variableName,
      @ParamName("newValue") CallArgument newValue,
      @ParamName("callFrameId") String callFrameId);

  /**
   * Enables or disables async call stacks tracking.
   *
   * @param maxDepth Maximum depth of async call stacks. Setting to <code>0</code> will effectively
   *     disable collecting async call stacks (default).
   */
  void setAsyncCallStackDepth(@ParamName("maxDepth") Integer maxDepth);

  /**
   * Replace previous blackbox patterns with passed ones. Forces backend to skip stepping/pausing in
   * scripts with url matching one of the patterns. VM will try to leave blackboxed script by
   * performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
   *
   * @param patterns Array of regexps that will be used to check script url for blackbox state.
   */
  @Experimental
  void setBlackboxPatterns(@ParamName("patterns") List<String> patterns);

  /**
   * Makes backend skip steps in the script in blackboxed ranges. VM will try leave blacklisted
   * scripts by performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
   * Positions array contains positions where blackbox state is changed. First interval isn't
   * blackboxed. Array should be sorted.
   *
   * @param scriptId Id of the script.
   * @param positions
   */
  @Experimental
  void setBlackboxedRanges(
      @ParamName("scriptId") String scriptId,
      @ParamName("positions") List<ScriptPosition> positions);

  /**
   * Fired when virtual machine parses script. This event is also fired for all known and
   * uncollected scripts upon enabling debugger.
   */
  @EventName("scriptParsed")
  EventListener onScriptParsed(EventHandler<ScriptParsed> eventListener);

  /** Fired when virtual machine fails to parse the script. */
  @EventName("scriptFailedToParse")
  EventListener onScriptFailedToParse(EventHandler<ScriptFailedToParse> eventListener);

  /** Fired when breakpoint is resolved to an actual script and location. */
  @EventName("breakpointResolved")
  EventListener onBreakpointResolved(EventHandler<BreakpointResolved> eventListener);

  /**
   * Fired when the virtual machine stopped on breakpoint or exception or any other stop criteria.
   */
  @EventName("paused")
  EventListener onPaused(EventHandler<Paused> eventListener);

  /** Fired when the virtual machine resumed execution. */
  @EventName("resumed")
  EventListener onResumed(EventHandler<Resumed> eventListener);
}