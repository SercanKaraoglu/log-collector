environments {
	dev {
		server {
			name='dev'
			bucket = 'app/conf/file-reader-dev/'
		}
	}
	test {
		server {
			name='test'
			bucket = 'app/conf/file-reader-test/'
		}
	}
	uat1 {
		server {
			name='uat1'
			bucket = 'app/conf/file-reader-uat1/'
		}
	}
}