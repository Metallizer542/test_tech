pipeline {
	agent any
	stages {

	 	stage ('check hadoop services'){
	 		steps{
	 			script{
	 				getResponseCode('Ranger','http://192.168.1.21:6080')
	 				getResponseCode('YARN','http://192.168.1.21:8088')	 		 				
	 			}
			}
		}

		stage ('install nginx'){
			steps{
				script{
					echo 'Install nginx with ansible'
					sh 'ansible-playbook /opt/ansible/nginx.yml'
	 	   			echo 'Check nginx service'
	 	   			getResponseCode('nginx','http://192.168.1.21:80')

				}
			}
		}
	}
}	

def getResponseCode (ServiceName,URL){
	def ResponceCode = sh(script:'curl -s -o /dev/null -I -w "%{http_code}" -L ' + URL,returnStdout: true).trim()
	echo "Response code from service ${ServiceName} is ${ResponceCode} "

	if (ResponceCode == '200') {
		echo "${ServiceName} is OK"

	}else if (ResponceCode == '405') {
		echo 'try to send POST request'
		ResponceCode = sh(script:'curl -X POST -s -o /dev/null -I -w "%{http_code}" -L ' + URL,returnStdout: true).trim()

		if (ResponceCode == '200') {
			echo "${ServiceName} is OK"
		}else{
			error("${ServiceName} send wrong http code (${ResponceCode})")
		}
		
	}
	else{
		error("${ServiceName} send wrong http code (${ResponceCode})")
	}
	return ResponceCode;
}


