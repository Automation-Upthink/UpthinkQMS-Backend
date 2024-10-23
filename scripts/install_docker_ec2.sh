#!/bin/bash

# Update the package index
sudo apt-get update

# Install prerequisite packages
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker's official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Set up the stable repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Update the package index again
sudo apt-get update





# Install Docker Engine
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Add your user to the docker group (replace 'your_username' with your username)
sudo usermod -aG docker your_username

# Apply new group membership
newgrp docker

# Enable Docker service to start on boot
sudo systemctl enable docker

# Verify Docker installation
docker info



# sudo apt install apt-transport-https ca-certificates curl software-properties-common -y

# sudo systemctl start docker
# sudo systemctl enable docker
# sudo usermod -aG docker $USER