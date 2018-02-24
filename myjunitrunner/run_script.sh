#! /bin/sh
export CLASSPATH=myjunitrunner.jar:lib/junit.jar:lib/asm-debug-all-5.1.jar
task_dir=$1
task_classpath=$(ls $task_dir/target/*.jar $task_dir/target/dependency/*.jar | tr '\n' ':')
export CLASSPATH=$CLASSPATH:$task_classpath:$task_dir/target/classes:$task_dir/target/test-classes:$task_dir/src/main/java:$task_dir/src/main/resources:$task_dir/src/test/java:$task_dir/src/test/resources

# find target classes
rm $task_dir/target/targetClasses.txt
java org.qianqianzhu.instrument.util.FindTargetClasses $task_dir/target

while IFS='' read -r line || [[ -n "$line" ]]; do
    >&2 echo "loading: $line"
    java -javaagent:./lib/SimpleMutationInstrument.jar -noverify org.qianqianzhu.junit.MyJunitRunner $line $task_dir/target/test-classes $task_dir/report/manual/$line true
done < $task_dir/target/targetClasses.txt
