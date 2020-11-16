package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
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

public class GetWhatever {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-boot.git";

    private static final String path_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String path_rear = "/boot";

    public static void main(String[] args) throws IOException, GitAPIException{

        File newDir = new File(path_head + path_rear);
        newDir.mkdirs();
        File localPath = new File(path_head + path_rear);
        localPath.createNewFile();

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            Collection<Ref> allRefs = git.getRepository().getAllRefs().values();

            int counter = 0; // count the number of commits in one repository
            ArrayList<ObjectId> allCommitIDs = new ArrayList<>(); // store the ID of each commit

            // get all the commits
            try(RevWalk revWalk = new RevWalk(git.getRepository())){

                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }

                for(RevCommit commit: revWalk){
                    allCommitIDs.add(commit.getId());
                    counter++;
                }

            }

            // get all the diff entries
            for(int i = allCommitIDs.size() - 1; i >= 1; i--){
                ObjectReader reader = git.getRepository().newObjectReader();

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                ObjectId oldTree = git.getRepository().resolve(allCommitIDs.get(i).getName() + "^{tree}");
                oldTreeIter.reset(reader, oldTree);

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                ObjectId newTree = git.getRepository().resolve(allCommitIDs.get(i - 1).getName() + "^{tree}");
                newTreeIter.reset(reader, newTree);

                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                diffFormatter.setRepository(git.getRepository());
                List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

                for(DiffEntry entry: entries){
                    System.out.println(entry.getChangeType() + "  " + entry.getOldPath() + "  " + entry.getNewPath());
                }
            }

            //TODO: extract files' names
            //TODO: get the content of files

        }

        FileUtils.deleteDirectory(localPath);

    }

}
