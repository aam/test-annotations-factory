package com.test.ap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.SourcePosition;

public class TestAnnotationProcessor implements AnnotationProcessor
{
 
	private AnnotationProcessorEnvironment environment;
 
	private AnnotationTypeDeclaration noteDeclaration;
 
	public TestAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		environment = env;
		// get the annotation type declaration for our 'Note' annotation.
		// Note, this is also passed in to our annotation factory - this 
		// is just an alternate way to do it.
		noteDeclaration = (AnnotationTypeDeclaration) environment.getTypeDeclaration("com.test.ap.Note");
	}
 

	class StringToAdd
	{
		int line;
		String string;
		
		StringToAdd(int line, String string) { this.line = line; this.string = string; }
	}

	public void process()
	{
 		HashMap<File, List<StringToAdd>> strings = new HashMap<File, List<StringToAdd>>();
		
		//
		// look for private properties and create __trojan__get public get methods
		//
 		
		for (TypeDeclaration td: this.environment.getSpecifiedTypeDeclarations())
		{
			HashSet<String> fieldnames = new HashSet<String>();
			for (MethodDeclaration fd: td.getMethods())
				fieldnames.add(fd.getSimpleName());
			
			for (FieldDeclaration fd: td.getFields())
				if (!fd.getSimpleName().startsWith("__trojan__get_"))
				{
					boolean isPublic = false;
					for (com.sun.mirror.declaration.Modifier m: fd.getModifiers())
					{
						if (m == Modifier.PUBLIC)
						{
							isPublic = true;
							break;
						}
					}
				
					String trojanFieldName = "__trojan__get_" + fd.getSimpleName();
					if (!isPublic && !fieldnames.contains(trojanFieldName))
					{
						//
						//	adding public get method for fd declaration
						//
						java.io.File srcFile = fd.getPosition().file();
						int line = fd.getPosition().line();
					
						List<StringToAdd> s = strings.get(srcFile);
						if (s == null)
						{
							s = new ArrayList<StringToAdd>();
							strings.put(srcFile, s);
						}
						s.add(new StringToAdd(line, String.format("%1$100s %2$s %3$s() { return %4$s; } ", "public", fd.getType(), trojanFieldName, fd.getSimpleName())));
						fieldnames.add(trojanFieldName);
					}
				}
		}
		
		for (File f: strings.keySet())
			addLinesToFile(f, strings.get(f));
		

		// Get all declarations that use the note annotation.
		Collection<Declaration> declarations = environment
				.getDeclarationsAnnotatedWith(noteDeclaration);
		for (Declaration declaration : declarations) {
			processTestAnnotations(declaration);
		}
	}
 	
	private void processTestAnnotations(Declaration declaration) {
		// Get all of the annotation usage for this declaration.
		// the annotation mirror is a reflection of what is in the source.
		Collection<AnnotationMirror> annotations = declaration.getAnnotationMirrors();
		// iterate over the mirrors.
		for (AnnotationMirror mirror : annotations) {
			// if the mirror in this iteration is for our note declaration...
			if(mirror.getAnnotationType().getDeclaration().equals(
					noteDeclaration)) {
				
				// print out the goodies.
				SourcePosition position = mirror.getPosition();
				Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror
						.getElementValues();
 
				System.out.println("Declaration: " + declaration.toString());
				System.out.println("Position: " + position);
				System.out.println("Values:");
				for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : values
						.entrySet()) {
					AnnotationTypeElementDeclaration elemDecl = entry.getKey();
					AnnotationValue value = entry.getValue();
					System.out.println("    " + elemDecl + "=" + value);
				}
			}
		}
	}

	private void addLinesToFile(File srcFile, List<StringToAdd> strings)
	{
		try
		{
			File newf = File.createTempFile("eclipse", "trojan");
			BufferedReader is = new BufferedReader(new FileReader(srcFile));
			PrintWriter p = new PrintWriter(new FileWriter(newf));
			
			String lin;
			int cLine = 1;
			Iterator<StringToAdd> itString = strings.iterator();
			StringToAdd cStringToAdd = itString.hasNext()? itString.next(): null;
			boolean anyUpdates = false;
			while ((lin = is.readLine()) != null)
			{
				if (cStringToAdd != null && cLine > cStringToAdd.line)
				{
					if (lin.compareTo(cStringToAdd.string) != 0)
					{
						p.println(cStringToAdd.string);
						anyUpdates = true;
					}
					cStringToAdd = itString.hasNext()? itString.next(): null;
				}
				p.println(lin);
				cLine++;
			}
			is.close();
			p.close();
		
			if (anyUpdates)
			{
				File tempfile = new File(srcFile.getCanonicalPath() + ".tmp");
				srcFile.renameTo(tempfile);
				newf.renameTo(srcFile);
				tempfile.delete();
			}
			
		} catch (IOException e)
		{
			System.out.println("IOException: " + e);
		}
	}
}