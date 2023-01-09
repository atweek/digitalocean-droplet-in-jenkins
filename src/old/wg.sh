apt update
apt install -y wireguard
ip a
echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
sysctl -p
