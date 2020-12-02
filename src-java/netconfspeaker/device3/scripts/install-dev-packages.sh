#!/bin/sh
set -e

# install developer stuff
apk add vim tree bash git bash-completion gcc make readline-dev musl-dev
apk add --allow-untrusted --repository /pkgs \
    libyang-dev libyang-cpp  libyang-doc \
    sysrepo-dev sysrepo-cpp

wget https://github.com/nathanalderson/yang.vim/archive/master.zip
mkdir -p $HOME/.vim/pack/plugins/start 
unzip master.zip -d $HOME/.vim/pack/plugins/start 
rm -v master.zip

cat > $HOME/.vimrc <<EOF
syntax on
set filetype=on
set tabstop=4
set shiftwidth=4
set mouse=r
set expandtab
set autoindent
set splitbelow
set splitright

set enc=utf-8
set fileencoding=utf-8
set fileencodings=ucs-bom,utf8,prc
set guifont=Monaco:h11
set guifontwide=NSimsun:h12
set visualbell
set number

" YAML
autocmd FileType yaml setlocal ts=2 sts=2 sw=2 expandtab
" YANG
autocmd FileType yang setlocal ts=2 sts=2 sw=2 expandtab
" Makefile
autocmd FileType make setlocal noexpandtab
EOF

echo 'alias ll="ls -lhF --color=auto --group-directories-first"' >> $HOME/.bashrc
echo 'PS1="\w# "' >> $HOME/.bashrc
exec bash
