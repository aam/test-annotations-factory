package com.test.ap;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
 
public class TestAnnotationProcessorFactory implements
		AnnotationProcessorFactory {
 
	/**
	 * Returns a note annotation processor.
	 * 
	 * @return An annotation processor for note annotations if requested, 
	 * otherwise, returns the NO_OP annotation processor.
	 */
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> declarations,
			AnnotationProcessorEnvironment env) {
		AnnotationProcessor result;
		if(declarations.isEmpty()) {
			result = AnnotationProcessors.NO_OP;
		}
		else {
			// Next Step - implement this class:
			result = new TestAnnotationProcessor(env);
		}
		return result;
 
	}
 
	/**
	 * This factory only builds processors for the 
	 * {@link com.javalobby.tnt.annotation.Note} annotation.
	 * @return a collection containing only the note annotation name.
	 */
	public Collection<String> supportedAnnotationTypes() {
		return Collections.singletonList("com.test.ap.Note");
	}
 
	/**
	 * No options are supported by this annotation processor.
	 * @return an empty list.
	 */
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}
}