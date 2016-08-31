# Log Collector

This application broadcast lines from log file. Deployment can be made async, which means log reader or log consumer can be deployed independently because of service discovery feature.

### Build Image

While this will build docker container it also produces distribution .zip, .tar and start scripts as well under the build directory.

```{r, engine='groovy', count_lines}
$ gradle distDocker
```

###AWS S3 Integration
This project have S3 integration if you want to use it then aws profile need to be defined. If we pass docker environment variables like this; 
-e profile=s3 -e region=us-east-1 -e path=sercan-deneme 
Then we need to define s3 profile
under the ~/.aws/credentials like following

```
[s3]
aws_access_key_id = your aws id 
aws_secret_access_key = your aws access key
```

### Files of Interest
Each local file reader instance expose whatever file under the path of /data , while S3 file readers expose files under the given path with environment variable path, in the below case
it is sercan-deneme. However, only those files that given with -e files parameter will be consumed by the application, in the below clustered case they are first.log, second.log and cloud.log

#### Clustered Run 

Example clustered configuration includes 4 JVM; runs two local path server and one s3 path server and one zookeeper for service discovery. 
Local path servers serves ${projectDir}/data/first and ${projectDir}/data/second.  

```{r, engine='groovy', count_lines}
$ cd app/clustered
$ docker-compose up
```

### Non Clustered (Single JVM) Run

```{r, engine='groovy', count_lines}
$ cd app/nonclustered
$ bash run.sh
```
