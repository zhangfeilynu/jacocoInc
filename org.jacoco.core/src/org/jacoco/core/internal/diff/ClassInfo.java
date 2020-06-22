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

import java.util.List;

public class ClassInfo {

	private String classFile;

	private String className;

	private String packages;

	private List<MethodInfo> methodInfos;

	private List<int[]> addLines;

	private List<int[]> delLines;

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<int[]> getAddLines() {
		return addLines;
	}

	public void setAddLines(List<int[]> addLines) {
		this.addLines = addLines;
	}

	public List<int[]> getDelLines() {
		return delLines;
	}

	public void setDelLines(List<int[]> delLines) {
		this.delLines = delLines;
	}

	public String getClassFile() {
		return classFile;
	}

	public void setClassFile(String classFile) {
		this.classFile = classFile;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackages() {
		return packages;
	}

	public void setPackages(String packages) {
		this.packages = packages;
	}

	public List<MethodInfo> getMethodInfos() {
		return methodInfos;
	}

	public void setMethodInfos(List<MethodInfo> methodInfos) {
		this.methodInfos = methodInfos;
	}
}
