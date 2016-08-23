#!/bin/bash
## doxygenize

doxygen Doxyfile

cd html
git push origin gh-pages
cd ..