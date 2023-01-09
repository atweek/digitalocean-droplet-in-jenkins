pipeline {
    agent any
    parameters {
        choice(name: 'location', choices: ['nyc1','nyc3','ams3','sfo3','sgp1','lon1','fra1','tor1','blr1','syd1'], description: 'A slug indicating the region where the Droplet will be createdi')

	choice(name: 'size', choices: ["1 CPU 1 GB","1 CPU 0.5 GB"], description: 'A slug indicating the size of the Droplet')

	string(name: 'time', description: 'Через сколько удвлить машину?(минуты)')
    }
    stages {
        stage('auth') {
            environment {
                TOKEN = credentials('36d3b42a-d868-4709-bb29-0be3986cc994')
	   }//env
            steps {
		script {
		    echo "account"
		    sh 'uname -a'
	            sh 'doctl auth init -t $TOKEN'
                    sh 'doctl account get'
                 }//script
	     }//steps
        }//stage
	stage('create VM') {
	    environment {
                SSH = credentials('ssh-jen')
	    }//env
            steps {
                script {
		//NYC1   New York City, United States   nyc1
		//NYC3   New York City, United States   nyc3
		//AMS3   Amsterdam, the Netherlands   ams3
		//SFO3   San Francisco, United States   sfo3
		//SGP1   Singapore   sgp1
		//LON1   London, United Kingdom   lon1
		//FRA1   Frankfurt, Germany   fra1
		//TOR1   Toronto, Canada   tor1
		//BLR1   Bangalore, India   blr1
		//SYD1   Sydney, Australia   syd1
		// doctl compute droplet create --region sfo2 --image ubuntu-18-04-x64 --size s-1vcpu-1gb <DROPLET-NAME>
			if (params.size == "1 CPU 1 GB")
			{
				size = "s-1vcpu-1gb"
			}
			else
			{
				size = "s-1vcpu-512mb-10gb"
			}
			//sh """
			//	whoami
			//	doctl compute droplet create --region ${params.location} --image ubuntu-22-04-x64 --ssh-keys ${SSH} --size ${size} jen-auto
			//"""
			//sleep time: "2", unit: 'MINUTES'
			sh """
				doctl compute droplet get --template {{.PublicIPv4}} jen-auto
				echo "[servers]" > ./src/inventory.ini
				doctl compute droplet get --template {{.PublicIPv4}} jen-auto >> ./src/inventory.ini
			"""
                 }//script
	     }//steps
        }//stage
         stage('install wg') {
            steps {
	        script {
		    ansiColor('xterm') {
   		    	ansiblePlaybook( 
                    	playbook: './src/playbook.yml',
                    	inventory: './src/inventory.ini',
			disableHostKeyChecking: true,
			//becomeUser: 'root', 
                    	credentialsId: '/home/atweek/.ssh/id_rsa.pub',
  			sudoUser: 'root',
        	    	colorized: true) 
                    }
                 }//script
             }//steps
        }//stage
	stage('wait') {
            steps {
	         script {
			sleep time: "${params.time}", unit: 'MINUTES'	
                 }//script
             }//steps
        }//stage

        stage('remove') {
            steps {
	         script {
			sh """
			doctl compute droplet delete jen-auto -f
			"""
                 }//script
             }//steps
        }//stage
    }//stages
}//end
