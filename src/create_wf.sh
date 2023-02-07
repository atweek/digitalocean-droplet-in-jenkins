user_key=$(wg genkey)
root_key=$(wg genkey)

user_key_pub=echo $(echo $user_key | wg pubkey)
root_key_pub=echo $(echo $root_key | wg pubkey)

cat << EOF >> /etc/wireguard/wg0.conf
[Interface]
PrivateKey = $root_key_pub
Address = 10.0.0.1/24
ListenPort = 84344
PostUp = iptables -A FORWARD -i %i -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i %i -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

[Peer]
PublicKey = $user_key_pub
AllowedIPs = 10.0.0.2/32
EOF

cat << EOF > ~/user.conf
[Interface]
Address = 10.0.0.2/24
DNS = 8.8.8.8
PrivateKey = $user_key
[Peer]
PublicKey = $root_key_pub
AllowedIPs = 0.0.0.0/0
Endpoint = $(ip addr show | egrep "inet.*\d.* scope global eth1" | grep -Eoh "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\/[0-9]{1,3}")
PersistentKeepalive = 25
EOF
