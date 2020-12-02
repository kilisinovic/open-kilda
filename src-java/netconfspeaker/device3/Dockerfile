# netmeister image
ARG     IMAGE=alpine
ARG     VERSION=3.9
# build
FROM    ${IMAGE}:${VERSION}
ARG     BASE=registry.gitlab.flint.si/ps/telstra/kilda/alpine/builder
ARG     PACKAGES="libyang libyang-dev py-libyang sysrepo sysrepo-dev py-sysrepo netopeer2"
COPY    --from=registry.gitlab.flint.si/ps/telstra/kilda/alpine/builder \
            /sw/packages/ /pkgs
COPY    --from=registry.gitlab.flint.si/ps/telstra/kilda/alpine/builder \
            /var/lib/sysrepo/ /var/lib/sysrepo/
RUN     apk add --allow-untrusted --repository /pkgs ${PACKAGES} && \
            adduser -D admin admin && \
            echo admin:admin | chpasswd
ENV     NMSHOME         /srv
WORKDIR ${NMSHOME}
COPY     .  .
# customize
RUN     mv entrypoint.sh /sbin/entrypoint.sh && \
        for home in /root /home/admin; do \
            mkdir -p $HOME/.ssh $HOME/.netopeer2-cli && \
            cp -v config.xml $HOME/.netopeer2-cli/config.xml && \
            echo 'connect --unix' > $HOME/.netopeer2-cli/history; \
        done && \
        find yang/ \
            -type f \
            -exec sysrepoctl -i {} -a \; && \
        find startup/ \
            -type f \
            -exec sysrepocfg --edit={} -d startup \; && \
        find yang/ startup/ . \
            -maxdepth 1 \
            -type f \
            -print -delete
# finalize
ENTRYPOINT  /sbin/entrypoint.sh
VOLUME      /var/lib/sysrepo
EXPOSE      830
