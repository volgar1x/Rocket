package org.rocket.guice;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class RocketModule implements Module {
    private Binder binder;

    protected void before() {}
    protected abstract void configure();
    protected void after() {}
    
    @Override
    public final void configure(Binder binder) {
        this.binder = binder;
        before();
        try {
            configure();
        } finally {
            after();
            this.binder = null;
        }
    }
    
    protected Binder binder() {
        return binder;
    }

    protected void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
        binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
    }

    protected <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return binder.getMembersInjector(typeLiteral);
    }

    protected void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        binder.bindListener(typeMatcher, listener);
    }

    protected void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        binder.bindScope(annotationType, scope);
    }

    protected void install(Module module) {
        binder.install(module);
    }

    protected void addError(Throwable t) {
        binder.addError(t);
    }

    protected Binder withSource(Object source) {
        return binder.withSource(source);
    }

    protected void requestInjection(Object instance) {
        binder.requestInjection(instance);
    }

    protected Stage currentStage() {
        return binder.currentStage();
    }

    protected void addError(Message message) {
        binder.addError(message);
    }

    protected AnnotatedConstantBindingBuilder bindConstant() {
        return binder.bindConstant();
    }

    protected <T> void requestInjection(TypeLiteral<T> type, T instance) {
        binder.requestInjection(type, instance);
    }

    protected <T> Provider<T> getProvider(Key<T> key) {
        return binder.getProvider(key);
    }

    protected Binder skipSources(Class... classesToSkip) {
        return binder.skipSources(classesToSkip);
    }

    protected <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return binder.getMembersInjector(type);
    }

    protected <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        return binder.bind(key);
    }

    protected void addError(String message, Object... arguments) {
        binder.addError(message, arguments);
    }

    protected PrivateBinder newPrivateBinder() {
        return binder.newPrivateBinder();
    }

    protected <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return binder.bind(typeLiteral);
    }

    protected void requireExplicitBindings() {
        binder.requireExplicitBindings();
    }

    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
    }

    protected void requestStaticInjection(Class<?>... types) {
        binder.requestStaticInjection(types);
    }

    protected <T> Provider<T> getProvider(Class<T> type) {
        return binder.getProvider(type);
    }

    protected void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
        binder.convertToTypes(typeMatcher, converter);
    }

    protected void disableCircularProxies() {
        binder.disableCircularProxies();
    }
}
