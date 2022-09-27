#!/bin/bash

while read i;
do
  imageName=$(basename ${i} "png")
  thumbName="${imageName}thumb.png"
  convert -geometry x300 ${i} ${thumbName}
done < <(ls -1 *.png | grep -v "thumb")
