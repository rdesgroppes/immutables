/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.mongo;

import com.google.common.collect.Iterables;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.backend.KeyExtractor;
import org.immutables.criteria.expression.Visitors;

import java.lang.reflect.Member;
import java.util.Objects;

/**
 * Allows to query and modify mongo documents using criteria API.
 *
 * <p>Based on <a href="https://mongodb.github.io/mongo-java-driver-reactivestreams/">Mongo reactive streams driver</a>
 */
public class MongoBackend implements Backend {

  private final CollectionResolver collectionResolver;
  private final KeyExtractor.Factory keyExtractorFactory;

  public MongoBackend(MongoSetup setup) {
    Objects.requireNonNull(setup, "setup");
    this.collectionResolver = setup.collectionResolver();
    this.keyExtractorFactory = setup.keyExtractorFactory();
  }

  @Override
  public Session open(Class<?> entityType) {
    Objects.requireNonNull(entityType, "context");
    KeyExtractor keyExtractor = keyExtractorFactory.create(entityType);
    Member idProperty;
    if (keyExtractor.metadata().isExpression() && keyExtractor.metadata().isKeyDefined()) {
      idProperty = (Member) Visitors.toPath(Iterables.getOnlyElement(keyExtractor.metadata().keys())).element();
    } else {
      idProperty = null;
    }
    MongoPathNaming naming = new MongoPathNaming(idProperty);
    return new MongoSession(collectionResolver.resolve(entityType), keyExtractor, naming);
  }

}
