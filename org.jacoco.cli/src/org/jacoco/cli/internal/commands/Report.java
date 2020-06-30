/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Keeping - initial implementation
 *    Marc R. Hoffmann - rework
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * The <code>report</code> command.
 */
public class Report extends Command {

	@Argument(usage = "list of JaCoCo *.exec files to read", metaVar = "<execfiles>")
	List<File> execfiles = new ArrayList<File>();

	@Option(name = "--classfiles", usage = "location of Java class files", metaVar = "<path>", required = true)
	List<File> classfiles = new ArrayList<File>();

	@Option(name = "--sourcefiles", usage = "location of the source files", metaVar = "<path>")
	List<File> sourcefiles = new ArrayList<File>();

	@Option(name = "--tabwith", usage = "tab stop width for the source pages (default 4)", metaVar = "<n>")
	int tabwidth = 4;

	@Option(name = "--name", usage = "name used for this report", metaVar = "<name>")
	String name = "JaCoCo Coverage Report";

	@Option(name = "--encoding", usage = "source file encoding (by default platform encoding is used)", metaVar = "<charset>")
	String encoding="UTF-8";

	@Option(name = "--xml", usage = "output file for the XML report", metaVar = "<file>")
	File xml;

	@Option(name = "--csv", usage = "output file for the CSV report", metaVar = "<file>")
	File csv;

	@Option(name = "--html", usage = "output directory for the HTML report", metaVar = "<dir>")
	File html;

	@Option(name = "--type", usage = "分析类型，1全量2分支3tag", metaVar = "<n>")
	int type=3;

	@Option(name = "--gitDir", usage = "本地git目录", metaVar = "<dir>")
	String gitDir=".";

	@Option(name = "--gitName", usage = "git用户名", metaVar = "<gitName>")
	String gitName="";

	@Option(name = "--gitPassword", usage = "git密码", metaVar = "<gitPassword>")
	String gitPassword="";

	@Option(name = "--branchName", usage = "分支名称，tag增量时使用该参数，默认master", metaVar = "<branchName>")
	String branchName="master";

	@Option(name = "--newTag", usage = "新tag(预发版本)，tag增量时使用该参数", metaVar = "<newTag>")
	String newTag="";

	@Option(name = "--oldTag", usage = "基线tag(变更前的版本)，tag增量时使用该参数", metaVar = "<oldTag>")
	String oldTag="";

	@Option(name = "--newBranchName", usage = "开发分支名称（预发分支），分支增量时使用该参数", metaVar = "<newBranchName>")
	String newBranchName="";

	@Option(name = "--oldBranchName", usage = "基线分支名称，分支增量时使用该参数，默认", metaVar = "<oldBranchName>")
	String oldBranchName="master";

	@Override
	public String description() {
		return "Generate reports in different formats by reading exec and Java class files.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final ExecFileLoader loader = loadExecutionData(out);
		if(type==1){
			final IBundleCoverage bundle = analyze1(loader.getExecutionDataStore(),
					out);
			writeReports(bundle, loader, out);
		}else if(type==2){
			final IBundleCoverage bundle = analyze2(loader.getExecutionDataStore(),
					out);
			writeReports(bundle, loader, out);
		}else{
			final IBundleCoverage bundle = analyze3(loader.getExecutionDataStore(),
					out);
			writeReports(bundle, loader, out);
		}
		return 0;
	}

	private ExecFileLoader loadExecutionData(final PrintWriter out)
			throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		if (execfiles.isEmpty()) {
			out.println("[WARN] No execution data files provided.");
		} else {
			for (final File file : execfiles) {
				out.printf("[INFO] Loading execution data file %s.%n",
						file.getAbsolutePath());
				loader.load(file);
			}
		}
		return loader;
	}

	private IBundleCoverage analyze3(final ExecutionDataStore data,
			final PrintWriter out) throws IOException {
		GitAdapter.setCredentialsProvider(gitName, gitPassword);
		final CoverageBuilder builder = new CoverageBuilder(gitDir,branchName,newTag,oldTag);
		final Analyzer analyzer = new Analyzer(data, builder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(builder.getNoMatchClasses(), out);
		return builder.getBundle(name);
	}

	private IBundleCoverage analyze2(final ExecutionDataStore data,
									 final PrintWriter out) throws IOException {
		GitAdapter.setCredentialsProvider(gitName, gitPassword);
		final CoverageBuilder builder = new CoverageBuilder(gitDir,newBranchName,oldBranchName);
		final Analyzer analyzer = new Analyzer(data, builder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(builder.getNoMatchClasses(), out);
		return builder.getBundle(name);
	}

	private IBundleCoverage analyze1(final ExecutionDataStore data,
									 final PrintWriter out) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(data, builder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(builder.getNoMatchClasses(), out);
		return builder.getBundle(name);
	}

	private void printNoMatchWarning(final Collection<IClassCoverage> nomatch,
			final PrintWriter out) {
		if (!nomatch.isEmpty()) {
			out.println(
					"[WARN] Some classes do not match with execution data.");
			out.println(
					"[WARN] For report generation the same class files must be used as at runtime.");
			for (final IClassCoverage c : nomatch) {
				out.printf(
						"[WARN] Execution data for class %s does not match.%n",
						c.getName());
			}
		}
	}

	private void writeReports(final IBundleCoverage bundle,
			final ExecFileLoader loader, final PrintWriter out)
			throws IOException {
		out.printf("[INFO] Analyzing %s classes.%n",
				Integer.valueOf(bundle.getClassCounter().getTotalCount()));
		final IReportVisitor visitor = createReportVisitor();
		visitor.visitInfo(loader.getSessionInfoStore().getInfos(),
				loader.getExecutionDataStore().getContents());
		visitor.visitBundle(bundle, getSourceLocator());
		visitor.visitEnd();
	}

	private IReportVisitor createReportVisitor() throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

		if (xml != null) {
			final XMLFormatter formatter = new XMLFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(xml)));
		}

		if (csv != null) {
			final CSVFormatter formatter = new CSVFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(csv)));
		}

		if (html != null) {
			final HTMLFormatter formatter = new HTMLFormatter();
			visitors.add(
					formatter.createVisitor(new FileMultiReportOutput(html)));
		}

		return new MultiReportVisitor(visitors);
	}

	private ISourceFileLocator getSourceLocator() {
		final MultiSourceFileLocator multi = new MultiSourceFileLocator(
				tabwidth);
		for (final File f : sourcefiles) {
			multi.add(new DirectorySourceFileLocator(f, encoding, tabwidth));
		}
		return multi;
	}

}
