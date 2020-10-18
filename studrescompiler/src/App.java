import java.io.*;
import java.util.*;
public class App {
    static int num = 0;
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
        return true; // OK
    }
    static ArrayList<File> merger; // list of files in that module
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
        exec_temp.add(0,"pdfunite");
        exec_temp.add("collated/"+ module + ".pdf");
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
            if (!s.contains("_"))
            try {
                merger = new ArrayList<>();
                getpdfs("/cs/studres/" + s + "/Lectures", s);
                mergepdfs(s);
            } catch (Exception e) {
                //TODO: handle exception
                System.out.println("attempted string = " + s + " and exception details " + e);
            }
            
        }
           // now copy to designated directory on OneDrive
        Process p2 = Runtime.getRuntime().exec(new String[]{"rclone", "copy", "collated", "standrews:CSpdfcollated"});
        int t = p2.waitFor(); // make sure that it executes    
    }
    public static void main(String[] args) throws Exception {
        getfolder();
  //      System.out.println("Hello, World!");
    }
}
