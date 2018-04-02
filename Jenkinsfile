pipeline {
  environment{
    COURSIER_CACHE="/var/jenkins_home/.coursier-cache"
  }
  agent {
    label 'master'
  }
  triggers {
    pollSCM('0 0 1 1 *')
  }
  stages {
    stage('Build') {
      steps {
        echo 'Build'
	    sh "${tool name: 'sbt', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt clean compile"
      }
    }
    stage('Test') {
      steps {
        echo 'Test'
        sh "${tool name: 'sbt', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt test"
      }
    }
    stage('Publish') {
      when {
        branch 'master'
      }
      steps {
        echo 'Publish'
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'streamarchitect-nexus',
            usernameVariable: 'NEXUS_USERNAME_VARIABLE', passwordVariable: 'NEXUS_PASSWORD_VARIABLE']]) {
          sh "${tool name: 'sbt', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt publish"
        }
      }
    }
  }
}