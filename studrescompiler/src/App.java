import java.io.*;
import java.util.*;

import javax.lang.model.util.ElementScanner6;
public class App {
    static int num = 0;
    static String loc = "/tmp/collated";
    // check for files we do NOT want to merge
    public static boolean pdflogic(String path, String module)
    {
        if ((path.charAt(0) == '.') || !path.contains(".pdf"))
        {
            return false; // obvious, and "." are often ghost files
        }
        else if (module.equals("CS3104") && !path.contains("1up"))
        {
            return false; // otherwise we get a 4up version
        }
        else if (module.equals("CS2001") && path.contains("4Up"))
        {
            return false; // 4up again
        }
        else if (module.equals("CS3302") && path.contains("-slides"))
        {
            return false; // because slides and print version are equivalent, but latter is easier to read
        }
        else if (module.equals("CS4105") && path.contains("Bonjour"))
        {
            return false; // does not appear to be well-formatted and hence causes issues
        }
        return true; // OK
    }
    static ArrayList<File> merger; // list of files in that module
    // term 1 modules; do not continue processing for term 2
    static List<String> term1 = Arrays.asList("CS1002", "CS1005", "CS2001", "CS2003", "CS2101", "CS3050", "CS3099", "CS3104", "CS3105", "CS3301", "CS3302", "CS4052", "CS4105", "CS4201", "CS4202", "CS4203", "CS4302", "CS4402", "CS5001", "CS5002", "CS5010", "CS5030", "CS5031", "CS5032", "CS5040", "CS5042", "IS5102", "IS5103");
    static boolean isTermTwo = true; // flip switch for seocnd term
    public static void mergepdfs(String module) throws IOException, InterruptedException
    {
        // does the core merging routine
        ArrayList<String> exec_temp = new ArrayList<>();
        // sort the files so that they merge by earliest date, most likely way to ensure old-new method
        Collections.sort(merger, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            } });
        if (module.contains("CS1002") || module.contains("CS2003") || module.contains("CS4402") || module.contains("CS5030") || module.contains("CS5010") || module.contains("CS4201"))
        {
            // use old method, due to rotation problems
            exec_temp.add("pdfunite");
        }
        else
        {
        // allow full bookmark features
        exec_temp.add("java");
        exec_temp.add("-jar");
        exec_temp.add("pdfmerger-1.2.2-jar-with-dependencies.jar"); // as it has full bookmark support
        }
        for (File s: merger)
        {
            if (s.isDirectory())
            continue; // no point
            if (pdflogic(s.getName(), module))
            {
            System.out.println(s.getAbsolutePath());
            exec_temp.add(s.getAbsolutePath());
            }
        }
        exec_temp.add(loc + "/" + module + ".pdf");
       //  Process process = Runtime.getRuntime().exec(exec_cmd);
        String[] exec_arr = new String[exec_temp.size()];
        for (int i = 0; i < exec_temp.size(); i++)
        {
            exec_arr[i] = exec_temp.get(i);
        }
        Process p = Runtime.getRuntime().exec(exec_arr);
    //    int t = p.waitFor();
      //  System.out.println("status = " + t);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
    public static void getpdfs(String path, String module) throws IOException, InterruptedException
    {
        File f = new File(path);
        File[] nametemp = f.listFiles();
        // oldest first!
        for (File t:nametemp)
        {
            if (t.isDirectory())
            getpdfs(t.getAbsolutePath(), module);
            else
            merger.add(t);
        }
    }
    public static void getfolder() throws InterruptedException, IOException
    {
        String[] names;
        File f = new File("/cs/studres");
        names = f.list();
        for (String s: names)
        {
            if (!s.contains("_")) {
                if (isTermTwo && term1.contains(s))
                {
                    // we do not want to reprocess them
                    continue;
                }
                try {
                    merger = new ArrayList<>();
                    getpdfs("/cs/studres/" + s + "/Lectures", s);
                    mergepdfs(s);
                } catch (Exception e) {
                    //TODO: handle exception
                    System.out.println("attempted string = " + s + " and exception details " + e);
                }
            }
        }
           // now copy to designated directory on OneDrive
       Process p2 = Runtime.getRuntime().exec(new String[]{"rclone", "copy", loc, "standrews:CSpdfcollated"});
       int t = p2.waitFor(); // make sure that it executes    
    }
    public static void main(String[] args) throws Exception {
        getfolder();
  //      System.out.println("Hello, World!");
    }
}
