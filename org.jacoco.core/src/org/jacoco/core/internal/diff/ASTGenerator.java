/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/

package org.jacoco.core.internal.diff;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import sun.misc.BASE64Encoder;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ASTGenerator {
	private String javaText;
	private CompilationUnit compilationUnit;

	public ASTGenerator(String javaText) {
		this.javaText = javaText;
		this.initCompilationUnit();
	}

	private void initCompilationUnit() {
		final ASTParser astParser = ASTParser.newParser(8);
		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		astParser.setCompilerOptions(options);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		astParser.setStatementsRecovery(true);
		astParser.setSource(javaText.toCharArray());
		compilationUnit = (CompilationUnit) astParser.createAST(null);
	}

	public String getPackageName() {
		if (compilationUnit == null) {
			return "";
		}
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		if (packageDeclaration == null) {
			return "";
		}
		String packageName = packageDeclaration.getName().toString();
		return packageName;
	}

	public TypeDeclaration getJavaClass() {
		if (compilationUnit == null) {
			return null;
		}
		TypeDeclaration typeDeclaration = null;
		final List<?> types = compilationUnit.types();
		for (final Object type : types) {
			if (type instanceof TypeDeclaration) {
				typeDeclaration = (TypeDeclaration) type;
				break;
			}
		}
		return typeDeclaration;
	}

	public MethodDeclaration[] getMethods() {
		TypeDeclaration typeDec = getJavaClass();
		if (typeDec == null) {
			return new MethodDeclaration[] {};
		}
		MethodDeclaration[] methodDec = typeDec.getMethods();
		return methodDec;
	}

	public List<MethodInfo> getMethodInfoList() {
		MethodDeclaration[] methodDeclarations = getMethods();
		List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
		for (MethodDeclaration method : methodDeclarations) {
			MethodInfo methodInfo = new MethodInfo();
			setMethodInfo(methodInfo, method);
			methodInfoList.add(methodInfo);
		}
		return methodInfoList;
	}

	public ClassInfo getClassInfo(List<MethodInfo> methodInfos,
			List<int[]> addLines, List<int[]> delLines) {
		TypeDeclaration typeDec = getJavaClass();
		if (typeDec == null || typeDec.isInterface()) {
			return null;
		}
		ClassInfo classInfo = new ClassInfo();
		classInfo.setClassName(getJavaClass().getName().toString());
		classInfo.setPackages(getPackageName());
		classInfo.setMethodInfos(methodInfos);
		classInfo.setAddLines(addLines);
		classInfo.setDelLines(delLines);
		classInfo.setType("REPLACE");
		return classInfo;
	}

	public ClassInfo getClassInfo() {
		TypeDeclaration typeDec = getJavaClass();
		if (typeDec == null || typeDec.isInterface()) {
			return null;
		}
		MethodDeclaration[] methodDeclarations = getMethods();
		ClassInfo classInfo = new ClassInfo();
		classInfo.setClassName(getJavaClass().getName().toString());
		classInfo.setPackages(getPackageName());
		classInfo.setType("ADD");
		List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
		for (MethodDeclaration method : methodDeclarations) {
			MethodInfo methodInfo = new MethodInfo();
			setMethodInfo(methodInfo, method);
			methodInfoList.add(methodInfo);
		}
		classInfo.setMethodInfos(methodInfoList);
		return classInfo;
	}

	public MethodInfo getMethodInfo(MethodDeclaration methodDeclaration) {
		MethodInfo methodInfo = new MethodInfo();
		setMethodInfo(methodInfo, methodDeclaration);
		return methodInfo;
	}

	private void setMethodInfo(MethodInfo methodInfo,
			MethodDeclaration methodDeclaration) {
		methodInfo.setMd5(MD5Encode(methodDeclaration.toString()));
		methodInfo.setMethodName(methodDeclaration.getName().toString());
		methodInfo.setParameters(methodDeclaration.parameters().toString());
	}

	public static String MD5Encode(String s) {
		String MD5String = "";
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			BASE64Encoder base64en = new BASE64Encoder();
			MD5String = base64en.encode(md5.digest(s.getBytes("utf-8")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return MD5String;
	}

	public static boolean isMethodExist(final MethodDeclaration method,
			final Map<String, MethodDeclaration> methodsMap) {

		if (!methodsMap.containsKey(
				method.getName().toString() + method.parameters().toString())) {
			return false;
		}
		return true;
	}

	public static boolean isMethodTheSame(final MethodDeclaration method1,
			final MethodDeclaration method2) {
		if (MD5Encode(method1.toString())
				.equals(MD5Encode(method2.toString()))) {
			return true;
		}
		return false;
	}
}
