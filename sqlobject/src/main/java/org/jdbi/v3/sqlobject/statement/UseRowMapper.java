/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject.statement;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizer;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation;

/**
 * Used to specify specific row mapper on a query method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@SqlStatementCustomizingAnnotation(UseRowMapper.Factory.class)
public @interface UseRowMapper
{
    /**
     * The class implementing {@link RowMapper}. It must have a no-arg constructor.
     * @return the class of row mapper to use.
     */
    Class<? extends RowMapper<?>> value();

    class Factory implements SqlStatementCustomizerFactory
    {
        @Override
        public SqlStatementCustomizer createForMethod(Annotation annotation, Class<?> sqlObjectType, Method method) {
            final UseRowMapper mapperAnnotation = (UseRowMapper) annotation;
            RowMapper<?> mapper;
            try {
                mapper = mapperAnnotation.value().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new UnableToCreateStatementException("Could not create mapper " + mapperAnnotation.value().getName(), e, null);
            }

            final ResultReturner returner = ResultReturner.forMethod(sqlObjectType, method);
            return q -> q.getConfig(SqlObjectStatementConfiguration.class)
                    .setReturner(() -> returner.result(((ResultBearing) q).map(mapper), q.getContext()));
        }
    }
}
