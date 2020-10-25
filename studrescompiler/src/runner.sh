# script to run every day, updating the collated PDF files regularly as a result
( mkdir /tmp/collated;javac App.java;java App;rm -r /tmp/collated) || { echo "Script run failed!"; exit 1; }
echo $0 | at -M now + 24 hours