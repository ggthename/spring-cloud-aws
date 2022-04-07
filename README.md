# spring-cloud-aws

AWS Secrets manager를 통해서 datasource username / password를 관리할 수 있습니다.

<사전설정>
1. Local에서 사용할 경우 aws cli 설치 및 환경설정이 필요합니다.
2. AWS에 배포하여 사용할 경우 IAM을 통하여 사용자/역할에 SecretsManagerReadWrite 권한부여가 되어야 사용할 수 있습니다.

<사용방법>
1. cloud.aws.secretsmanager.name: 에 현재 사용중인 AWS Secrets Manager의 ARN
2. AWS Secrets Manager에 username, password 설정
