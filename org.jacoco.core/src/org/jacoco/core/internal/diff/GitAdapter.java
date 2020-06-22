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

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.*;
import java.util.*;

public class GitAdapter {
	private Git git;
	private Repository repository;
	private String gitFilePath;

	private static UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider;

	public GitAdapter(String gitFilePath) {
		this.gitFilePath = gitFilePath;
		this.initGit(gitFilePath);
	}

	private void initGit(String gitFilePath) {
		try {
			git = Git.open(new File(gitFilePath));
			repository = git.getRepository();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getGitFilePath() {
		return gitFilePath;
	}

	public Git getGit() {
		return git;
	}

	public Repository getRepository() {
		return repository;
	}

	public static void setCredentialsProvider(String username,
			String password) {
		if (usernamePasswordCredentialsProvider == null
				|| !usernamePasswordCredentialsProvider.isInteractive()) {
			usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(
					username, password);
		}
	}

	public String getBranchSpecificFileContent(String branchName,
			String javaPath) throws IOException {
		Ref branch = repository.exactRef("refs/heads/" + branchName);
		ObjectId objId = branch.getObjectId();
		RevWalk walk = new RevWalk(repository);
		RevTree tree = walk.parseTree(objId);
		return getFileContent(javaPath, tree, walk);
	}

	public String getTagRevisionSpecificFileContent(String tagRevision,
			String javaPath) throws IOException {
		ObjectId objId = repository.resolve(tagRevision);
		RevWalk walk = new RevWalk(repository);
		RevCommit revCommit = walk.parseCommit(objId);
		RevTree tree = revCommit.getTree();
		return getFileContent(javaPath, tree, walk);
	}

	private String getFileContent(String javaPath, RevTree tree, RevWalk walk)
			throws IOException {
		TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
		ObjectId blobId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(blobId);
		byte[] bytes = loader.getBytes();
		walk.dispose();
		return new String(bytes);
	}

	public AbstractTreeIterator prepareTreeParser(Ref localRef)
			throws IOException {
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(localRef.getObjectId());
		RevTree tree = walk.parseTree(commit.getTree().getId());
		CanonicalTreeParser treeParser = new CanonicalTreeParser();
		ObjectReader reader = repository.newObjectReader();
		treeParser.reset(reader, tree.getId());
		walk.dispose();
		return treeParser;
	}

	public void checkOut(String branchName) throws GitAPIException {

		git.checkout().setCreateBranch(false).setName(branchName).call();
	}

	public void checkOutAndPull(Ref localRef, String branchName)
			throws GitAPIException {
		boolean isCreateBranch = localRef == null;
		if (!isCreateBranch && checkBranchNewVersion(localRef)) {
			return;
		}

		git.checkout().setCreateBranch(isCreateBranch).setName(branchName)
				.setStartPoint("origin/" + branchName)
				.setUpstreamMode(
						CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
				.call();

		git.pull().setCredentialsProvider(usernamePasswordCredentialsProvider)
				.call();
	}

	private boolean checkBranchNewVersion(Ref localRef) throws GitAPIException {
		String localRefName = localRef.getName();
		String localRefObjectId = localRef.getObjectId().getName();

		Collection<Ref> remoteRefs = git.lsRemote()
				.setCredentialsProvider(usernamePasswordCredentialsProvider)
				.setHeads(true).call();
		for (Ref remoteRef : remoteRefs) {
			String remoteRefName = remoteRef.getName();
			String remoteRefObjectId = remoteRef.getObjectId().getName();
			if (remoteRefName.equals(localRefName)) {
				if (remoteRefObjectId.equals(localRefObjectId)) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
}
