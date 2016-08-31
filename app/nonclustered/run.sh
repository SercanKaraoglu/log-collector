 docker run -e conf=localConf.json \
            -e clustered=false \
            -e profile=s3 -e region=us-east-1 -e path=sercan-deneme \
            -e files='"first.log", "cloud.log"' \
            -v ${PWD}/../../data/first:/data \
            -v ~/.aws:/root/.aws \
            io.andromeda/log-collector