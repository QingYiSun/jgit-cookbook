package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetCommitsAndEntries {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-redis.git";

    private static final String pathName_1 = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String pathName_2 = "/tmp";

    public static void main(String[] args) throws IOException, GitAPIException{

        File d = new File(pathName_1 + pathName_2);
        d.mkdirs();

        File localPath = new File(pathName_1 + pathName_2);
        localPath.createNewFile();

        System.out.println("正在从：" + REMOTE_URL + "  克隆到：" + localPath);

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            System.out.println("已有仓库：" + git.getRepository().getDirectory());

            Collection<Ref> allRefs = git.getRepository().getAllRefs().values();
            System.out.println("Hello " + allRefs.size());

            int counter = 0;
            ArrayList<ObjectId> allCommitIDs = new ArrayList<>(); //to store all commits' identifications

            try(RevWalk revWalk = new RevWalk(git.getRepository())){

                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }
                System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);

                for(RevCommit commit: revWalk){
                   // System.out.println("Commit ID: " + commit.getId());
                   // System.out.println("; Commit full message: " + commit.getFullMessage());
                    allCommitIDs.add(commit.getId());
                    counter++;
                }

                System.out.println("获得了" + counter + "条commits，它们的ID分别为：");
                for(ObjectId id: allCommitIDs){
                    System.out.println(id);
                }

            }

            System.out.println(allCommitIDs.get(counter - 1).getName());

            ObjectReader reader = git.getRepository().newObjectReader();

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = git.getRepository().resolve(allCommitIDs.get(counter - 2).getName() + "^{tree}");
            oldTreeIter.reset(reader, oldTree);

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = git.getRepository().resolve(allCommitIDs.get(counter - 1).getName() + "^{tree}");
            newTreeIter.reset(reader, newTree);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            for(DiffEntry entry: entries){
                System.out.println(entry.getChangeType() + "  " + entry.getOldPath() + "  " + entry.getNewPath());
            }

        }

        FileUtils.deleteDirectory(localPath);

    }

}
