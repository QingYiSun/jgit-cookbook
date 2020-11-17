package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetWhatever {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-gemfire.git";

    private static final String path_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String path_rear = "/gemfire";

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
            ArrayList<String> CommitMSGs = new ArrayList<>(); // store all the commits' messages
            ArrayList<RevCommit> allCommits = new ArrayList<>(); // store all the commits

            // get all the commits
            try(RevWalk revWalk = new RevWalk(git.getRepository())){

                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }

                for(RevCommit commit: revWalk){
                    allCommits.add(commit);
                    CommitMSGs.add(commit.getFullMessage());
                    allCommitIDs.add(commit.getId());
                    counter++;
                }

            }

            // get all the diff entries
            for(int i = allCommitIDs.size() - 1; i >= 1; i--){
            //    System.out.println("Commit Info: " + allCommits.get(i).getName() + "  " + allCommits.get(i).getFullMessage());
            //    System.out.println("Commit Id: " + allCommitIDs.get(i).getName());
            //    System.out.println("Commit MSG: " + CommitMSGs.get(i));

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
                    if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY)) {
                        String tmpNew = readFile(git.getRepository(), allCommits.get(i), entry.getNewPath());
                        System.out.println("new: " + tmpNew);
                        String tmpOld = readFile(git.getRepository(), allCommits.get(i), entry.getOldPath());
                        System.out.println("old: " + tmpOld);
                    }
                    //TODO: identify NewPath & OldPath or former(i) & latter(i - 1)
                    //TODO: write all info into file defined by myself
                    /*
                    else if(entry.getChangeType().equals(DiffEntry.ChangeType.ADD)){

                    }
                    else if(entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)){

                    }
                    */
                }

            }

        }

        FileUtils.deleteDirectory(localPath);

    }

    private static String readFile(Repository repo, RevCommit commit, String filePath) throws IOException{
        try(TreeWalk walk = TreeWalk.forPath(repo, filePath, commit.getTree())){
            if(null != walk){
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
            else{
                throw new IllegalArgumentException("No path found.");
            }
        }
    }

}
