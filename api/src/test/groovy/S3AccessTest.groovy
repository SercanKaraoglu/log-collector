import io.andromeda.logcollector.S3FileReader
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by sercan on 25.11.2016.*/
class S3AccessTest extends Specification {
    
    @Shared
            s3reader = new S3FileReader("foreks", "eu-central-1")
    
    def "can list directory"() {
        when:
        def dir = s3reader.listDirectory("feed-backup-foreks/logs/pegasus-logs/input")
                          .collect({ -> [] }, { res, el -> res << el })
                          .toBlocking()
                          .single()
        then:
        dir.size() > 0
    }
    
    def "can read first line of gzip object"() {
        expect:
        "[06:10:47,200][0]BDBu;Dt20151201;" == s3reader.source("feed-backup-foreks/logs/pegasus-logs/input.log.2015-12-01.gz")
                                                       .toBlocking().first()
        
    }
}
