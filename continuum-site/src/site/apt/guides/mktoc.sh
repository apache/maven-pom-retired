#!/bin/sh

toc=`pwd`/index.apt

find . -name '*~' -exec rm -rf {} \;

#
# Top matter
#

echo " ------ " > $toc
echo " Summary of Continuum 1.x documentation " >> $toc
echo " ------ " >> $toc
echo " Jason van Zyl " >> $toc
echo " ------ " >> $toc
echo " 12 October 2005 " >> $toc
echo " ------ " >> $toc
echo >> $toc
echo "Documentation" >> $toc
echo >> $toc

#
# Getting started guide
#

echo "* Getting Started Guide" >> $toc
echo >> $toc
echo " * {{{getting-started/index.html}Getting Started Guide}}" >> $toc

#
# Mini Guides
#

echo >> $toc
echo "* Mini Guides" >> $toc
echo >> $toc

(
  cd mini

  for i in `ls -d guide-*`
  do
   if [ -d $i ]
   then
     (
       cd $i
       title=`grep "^ *Guide" ${i}.apt | sed 's/^ *//'`
       i=`echo $i | sed 's/\.apt/\.html/'`
       [ ! -z "$title" ] && echo " * {{{mini/$i/$i.html}$title}}" >> $toc && echo >> $toc
     )
   else
     title=`grep "^ Guide" $i | sed 's/^ *//'`
     i=`echo $i | sed 's/\.apt/\.html/'`
     [ ! -z "$title" ] && echo " * {{{mini/$i}$title}}" >> $toc && echo >> $toc
   fi
  
  done       
)       

#
# Introductions
#

echo >> $toc
echo "* Introductory Material" >> $toc
echo >> $toc

#
# Developer Guides
#

echo >> $toc
echo "* Development Guides" >> $toc
echo >> $toc

(
  cd development

  for i in `ls guide-*.apt`
  do
   title=`grep "^ Guide" $i | sed 's/^ *//'`
   i=`echo $i | sed 's/\.apt/\.html/'`
   [ ! -z "$title" ] && echo " * {{{development/$i}$title}}" >> $toc && echo >> $toc
  done       
)       
