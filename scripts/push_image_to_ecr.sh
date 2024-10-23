#export AWS_PROFILE=default
#
#aws ecr get-login-password --region ap-south-1 | \
#docker login --username AWS \
#--password-stdin 462203881643.dkr.ecr.ap-south-1.amazonaws.com

# Create a private repo: https://docs.aws.amazon.com/AmazonECR/latest/userguide/repository-create.html

docker build -f BaseDockerfile -t qms-base-dev .

docker tag qms-base-dev:latest 462203881643.dkr.ecr.ap-south-1.amazonaws.com/qms-base-dev:latest

docker push 462203881643.dkr.ecr.ap-south-1.amazonaws.com/qms-base-dev:latest