/*
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: https://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.linecorp.centraldogma.common.jsonpatch;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.linecorp.centraldogma.internal.Jackson;

/**
 * Base abstract class for one <a href="https://datatracker.ietf.org/doc/html/rfc6902">JSON
 * Patch</a> operation. A {@link JsonPatchOperation} can be converted into a JSON patch by calling
 * {@link #toJsonNode()}.
 *
 * <p><a href="https://datatracker.ietf.org/doc/html/rfc6902">JSON
 * Patch</a>, as its name implies, is an IETF draft describing a mechanism to
 * apply a patch to any JSON value. This implementation covers all operations
 * according to the specification; however, there are some subtle differences
 * with regards to some operations which are covered in these operations'
 * respective documentation.</p>
 *
 * <p>An example of a JSON Patch is as follows:</p>
 *
 * <pre>
 *     [
 *         {
 *             "op": "add",
 *             "path": "/-",
 *             "value": {
 *                 "productId": 19,
 *                 "name": "Duvel",
 *                 "type": "beer"
 *             }
 *         }
 *     ]
 * </pre>
 *
 * <p>This patch contains a single operation which adds an item at the end of
 * an array. A JSON Patch can contain more than one operation; in this case, all
 * operations are applied to the input JSON value in their order of appearance,
 * until all operations are applied or an error condition is encountered.</p>
 */
@JsonTypeInfo(use = Id.NAME, property = "op")
@JsonSubTypes({
        @Type(name = "add", value = AddOperation.class),
        @Type(name = "copy", value = CopyOperation.class),
        @Type(name = "move", value = MoveOperation.class),
        @Type(name = "remove", value = RemoveOperation.class),
        @Type(name = "removeIfExists", value = RemoveIfExistsOperation.class),
        @Type(name = "replace", value = ReplaceOperation.class),
        @Type(name = "safeReplace", value = SafeReplaceOperation.class),
        @Type(name = "test", value = TestOperation.class),
        @Type(name = "testAbsence", value = TestAbsenceOperation.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class JsonPatchOperation implements JsonSerializable {

    /**
     * Creates a new JSON Patch {@code add} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param value the value to add
     */
    public static AddOperation add(JsonPointer path, JsonNode value) {
        return new AddOperation(path, value);
    }

    /**
     * Creates a new JSON Patch {@code add} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param value the value to add
     */
    public static AddOperation add(String path, JsonNode value) {
        requireNonNull(path, "path");
        return add(JsonPointer.compile(path), value);
    }

    /**
     * Creates a new JSON Patch {@code copy} operation.
     *
     * @param from the source JSON Pointer
     * @param to the destination JSON Pointer
     */
    public static CopyOperation copy(JsonPointer from, JsonPointer to) {
        return new CopyOperation(from, to);
    }

    /**
     * Creates a new JSON Patch {@code copy} operation.
     *
     * @param from the source JSON Pointer
     * @param to the destination JSON Pointer
     */
    public static CopyOperation copy(String from, String to) {
        requireNonNull(from, "from");
        requireNonNull(to, "to");
        return copy(JsonPointer.compile(from), JsonPointer.compile(to));
    }

    /**
     * Creates a new JSON Patch {@code move} operation.
     *
     * @param from the source JSON Pointer
     * @param to the destination JSON Pointer
     */
    public static MoveOperation move(JsonPointer from, JsonPointer to) {
        return new MoveOperation(from, to);
    }

    /**
     * Creates a new JSON Patch {@code move} operation.
     *
     * @param from the source JSON Pointer
     * @param to the destination JSON Pointer
     */
    public static MoveOperation move(String from, String to) {
        requireNonNull(from, "from");
        requireNonNull(to, "to");
        return move(JsonPointer.compile(from), JsonPointer.compile(to));
    }

    /**
     * Creates a new JSON Patch {@code remove} operation.
     *
     * <p>Note that this operation will throw an exception if the path does not exist.
     *
     * @param path the JSON Pointer to remove
     */
    public static RemoveOperation remove(JsonPointer path) {
        return new RemoveOperation(path);
    }

    /**
     * Creates a new JSON Patch {@code remove} operation.
     *
     * <p>Note that this operation will throw an exception if the path does not exist.
     *
     * @param path the JSON Pointer to remove
     */
    public static RemoveOperation remove(String path) {
        requireNonNull(path, "path");
        return remove(JsonPointer.compile(path));
    }

    /**
     * Creates a new JSON Patch {@code removeIfExists} operation.
     *
     * @param path the JSON Pointer to remove if it exists
     */
    public static RemoveIfExistsOperation removeIfExists(JsonPointer path) {
        return new RemoveIfExistsOperation(path);
    }

    /**
     * Creates a new JSON Patch {@code removeIfExists} operation.
     *
     * @param path the JSON Pointer to remove if it exists
     */
    public static RemoveIfExistsOperation removeIfExists(String path) {
        requireNonNull(path, "path");
        return removeIfExists(JsonPointer.compile(path));
    }

    /**
     * Creates a new JSON Patch {@code replace} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param value the new value to replace the existing value
     */
    public static ReplaceOperation replace(JsonPointer path, JsonNode value) {
        return new ReplaceOperation(path, value);
    }

    /**
     * Creates a new JSON Patch {@code replace} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param value the new value to replace the existing value
     */
    public static ReplaceOperation replace(String path, JsonNode value) {
        requireNonNull(path, "path");
        return replace(JsonPointer.compile(path), value);
    }

    /**
     * Creates a new JSON Patch {@code safeReplace} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param oldValue the old value to replace
     * @param newValue the new value to replace the old value
     */
    public static SafeReplaceOperation safeReplace(JsonPointer path, JsonNode oldValue, JsonNode newValue) {
        return new SafeReplaceOperation(path, oldValue, newValue);
    }

    /**
     * Creates a new JSON Patch {@code safeReplace} operation.
     *
     * @param path the JSON Pointer for this operation
     * @param oldValue the old value to replace
     * @param newValue the new value to replace the old value
     */
    public static SafeReplaceOperation safeReplace(String path, JsonNode oldValue, JsonNode newValue) {
        requireNonNull(path, "path");
        return safeReplace(JsonPointer.compile(path), oldValue, newValue);
    }

    /**
     * Creates a new JSON Patch {@code test} operation.
     *
     * <p>This operation will throw an exception if the value at the path does not match the expected value.
     *
     * @param path the JSON Pointer for this operation
     * @param value the value to test
     */
    public static TestOperation test(JsonPointer path, JsonNode value) {
        return new TestOperation(path, value);
    }

    /**
     * Creates a new JSON Patch {@code test} operation.
     *
     * <p>This operation will throw an exception if the value at the path does not match the expected value.
     *
     * @param path the JSON Pointer for this operation
     * @param value the value to test
     */
    public static TestOperation test(String path, JsonNode value) {
        requireNonNull(path, "path");
        return test(JsonPointer.compile(path), value);
    }

    /**
     * Creates a new JSON Patch {@code testAbsent} operation.
     *
     * <p>This operation will throw an exception if the value at the path exists.
     *
     * @param path the JSON Pointer for this operation
     */
    public static TestAbsenceOperation testAbsence(JsonPointer path) {
        return new TestAbsenceOperation(path);
    }

    /**
     * Creates a new JSON Patch {@code testAbsent} operation.
     *
     * <p>This operation will throw an exception if the value at the path exists.
     *
     * @param path the JSON Pointer for this operation
     */
    public static TestAbsenceOperation testAbsence(String path) {
        requireNonNull(path, "path");
        return testAbsence(JsonPointer.compile(path));
    }

    /**
     * Converts {@link JsonPatchOperation}s to an array of {@link JsonNode}.
     */
    public static JsonNode asJsonArray(JsonPatchOperation... jsonPatchOperations) {
        requireNonNull(jsonPatchOperations, "jsonPatchOperations");
        return Jackson.valueToTree(jsonPatchOperations);
    }

    /**
     * Converts {@link JsonPatchOperation}s to an array of {@link JsonNode}.
     */
    public static JsonNode asJsonArray(Iterable<? extends JsonPatchOperation> jsonPatchOperations) {
        requireNonNull(jsonPatchOperations, "jsonPatchOperations");
        return Jackson.valueToTree(jsonPatchOperations);
    }

    private final String op;

    /*
     * Note: no need for a custom deserializer, Jackson will try and find a
     * constructor with a single string argument and use it.
     *
     * However, we need to serialize using .toString().
     */
    private final JsonPointer path;

    /**
     * Creates a new instance.
     *
     * @param op the operation name
     * @param path the JSON Pointer for this operation
     */
    JsonPatchOperation(final String op, final JsonPointer path) {
        this.op = requireNonNull(op, "op");
        this.path = requireNonNull(path, "path");
    }

    /**
     * Returns the operation name.
     */
    public final String op() {
        return op;
    }

    /**
     * Returns the JSON Pointer for this operation.
     */
    public final JsonPointer path() {
        return path;
    }

    /**
     * Applies this operation to a JSON value.
     *
     * @param node the value to patch
     * @return the patched value
     * @throws JsonPatchConflictException operation failed to apply to this value
     */
    public abstract JsonNode apply(JsonNode node);

    /**
     * Converts this {@link JsonPatchOperation} to a {@link JsonNode}.
     */
    public JsonNode toJsonNode() {
        return JsonNodeFactory.instance.arrayNode().add(Jackson.valueToTree(this));
    }

    JsonNode ensureExistence(JsonNode node) {
        final JsonNode found = node.at(path);
        if (found.isMissingNode()) {
            throw new JsonPatchConflictException("non-existent path: " + path);
        }
        return found;
    }

    static JsonNode ensureSourceParent(JsonNode node, JsonPointer path) {
        return ensureParent(node, path, "source");
    }

    static JsonNode ensureTargetParent(JsonNode node, JsonPointer path) {
        return ensureParent(node, path, "target");
    }

    private static JsonNode ensureParent(JsonNode node, JsonPointer path, String typeName) {
        /*
         * Check the parent node: it must exist and be a container (ie an array
         * or an object) for the add operation to work.
         */
        final JsonPointer parentPath = path.head();
        final JsonNode parentNode = node.at(parentPath);
        if (parentNode.isMissingNode()) {
            throw new JsonPatchConflictException("non-existent " + typeName + " parent: " + parentPath);
        }
        if (!parentNode.isContainerNode()) {
            throw new JsonPatchConflictException(typeName + " parent is not a container: " + parentPath +
                                                 " (" + parentNode.getNodeType() + ')');
        }
        return parentNode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JsonPatchOperation)) {
            return false;
        }
        final JsonPatchOperation that = (JsonPatchOperation) o;
        return op.equals(that.op) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, path);
    }

    @Override
    public abstract String toString();
}
