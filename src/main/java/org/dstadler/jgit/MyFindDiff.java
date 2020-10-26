package org.dstadler.jgit;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MyFindDiff {
    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-jpa.git";

    public static void main(String[] args) throws IOException, GitAPIException {
        File localPath = new File("/Users/abnerallen/jgit-cookbook/src/test/jpa");
   /*     localPath.createNewFile();

        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try(Git result = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .setProgressMonitor(new SimpleProgressMonitor())
            .call()
        ){
            System.out.println("Having repository: " + result.getRepository().getDirectory());

            Collection<Ref> allRefs = result.getRepository().getAllRefs().values();
            System.out.println("Hello   " + allRefs.size());
            try(RevWalk revWalk = new RevWalk(result.getRepository())){
                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }
                System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);
                int count = 0;
                for(RevCommit commit: revWalk){
                    System.out.println("Commit: " + commit);
                    count++;
                }
                System.out.println("Had " + count + " commits");
            }
            //Git: result, Repository: result.getRepository()
            listDiff(
                    result.getRepository(),
                    result,
                    "e8c5666070fc195dc6e9431a92013bff2ff926a0",
                    "85e588d7d033e4f9b818e4557e717f8102edc5c0"
            );
        }*/
        FileUtils.deleteDirectory(localPath);
    }

    private static class SimpleProgressMonitor implements ProgressMonitor {
        @Override
        public void start(int totalTasks){
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork){
            System.out.println("Start " + title + "; " + totalWork);
        }

        @Override
        public void update(int completed){
            System.out.println(completed + "-");
        }

        @Override
        public void endTask(){
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled(){
            return false;
        }
    }

    private static void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}
