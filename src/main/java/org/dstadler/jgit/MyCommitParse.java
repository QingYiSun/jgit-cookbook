package org.dstadler.jgit;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class MyCommitParse {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-jdbc.git";

    public static void main(String[] args) throws IOException, GitAPIException{
        File localPath = new File("/Users/abnerallen/jgit-cookbook/src/test/sd");
        /*
        localPath.createNewFile();
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try(Git result = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .setProgressMonitor(new SimpleProgressMonitor())
            .call()
        ){
            System.out.println("Having repository: " + result.getRepository().getDirectory());
            Collection<Ref> allRefs = result.getRepository().getAllRefs().values();
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
        }*/
        FileUtils.deleteDirectory(localPath);
    }

    private static class SimpleProgressMonitor implements ProgressMonitor{
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

}
