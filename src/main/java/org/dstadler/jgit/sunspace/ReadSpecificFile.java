package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.dstadler.jgit.helper.CookbookHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReadSpecificFile {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-gemfire.git";
    private static final String pathName_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String pathName_rear = "/gemfire";

    public static void main(String[] args) throws IOException, GitAPIException{

        // create temporary directory to store the git project
        File d = new File(pathName_head + pathName_rear);
        d.mkdirs();

        File localPath = new File(pathName_head + pathName_rear);
        localPath.createNewFile();

        // clone remote repository to local
        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            // resolve the lastCommitId
            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            System.out.println("ID resolved: " + lastCommitId.getName());

            // get the id of the last commit
            Collection<Ref> allRefs = git.getRepository().getAllRefs().values();
            int counter = 0;
            ArrayList<ObjectId> allCommitIDs = new ArrayList<>();
            try(RevWalk revWalk = new RevWalk(git.getRepository())){
                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }
                for(RevCommit commit: revWalk){
                    allCommitIDs.add(commit.getId());
                    counter++;
                }
            }

            ObjectReader reader = git.getRepository().newObjectReader();

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = git.getRepository().resolve(allCommitIDs.get(counter - 1100).getName() + "^{tree}");
            oldTreeIter.reset(reader, oldTree);
            System.out.println(allCommitIDs.get(counter - 1100).getName());

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = git.getRepository().resolve(allCommitIDs.get(counter - 1101).getName() + "^{tree}");
            newTreeIter.reset(reader, newTree);
            System.out.println(allCommitIDs.get(counter - 1101).getName());

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
