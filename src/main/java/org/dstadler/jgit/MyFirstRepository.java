package org.dstadler.jgit;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class MyFirstRepository {

    public static void main(String[] args) throws IOException, GitAPIException {
        File file = new File("/Users/abnerallen/jgit-cookbook/src/test/readme.txt");
        file.createNewFile();
        Git git = Git.init().setDirectory( file.getParentFile() ).call();
        System.out.println(git.getRepository().getDirectory());
    }

}
