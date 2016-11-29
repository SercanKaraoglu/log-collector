import io.andromeda.logcollector.S3FileReader
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by sercan on 25.11.2016.*/
class S3AccessTest extends Specification {
    
    @Shared
            s3reader = new S3FileReader("your-bucket", "your-region")
    
    def "can list directory"() {
        when:
        def dir = s3reader.listDirectory("path/to/logs")
                          .collect({ -> [] }, { res, el -> res << el })
                          .toBlocking()
                          .single()
        then:
        dir.size() > 0
    }
    
    def "can read first line of gzip object"() {
        expect:
        "Test" == s3reader.source("path/to/logs/input.log.2015-12-01.gz")
                          .toBlocking().first()
        
    }
}
