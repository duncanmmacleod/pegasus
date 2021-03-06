% Generated by roxygen2 (4.1.1): do not edit by hand
% Please edit documentation in R/job.R
\name{Uses}
\alias{Uses}
\title{Use of a logical file name}
\usage{
Uses(obj, arg, link = NULL, register = NULL, transfer = NULL,
  optional = NULL, namespace = NULL, version = NULL, executable = NULL,
  size = NULL)
}
\arguments{
\item{obj}{Object (Transformation or Job)}

\item{arg}{A string, an \code{Executable}, or a \code{File} representing the logical file}

\item{link}{Is this file a job input, output or both (See LFN) (optional)}

\item{register}{Should this file be registered in RLS? (True/False) (optional)}

\item{transfer}{Should this file be transferred? (True/False or See LFN) (optional)}

\item{optional}{Is this file optional, or should its absence be an error? (optional)}

\item{namespace}{Namespace of executable (optional)}

\item{version}{version of executable (optional)}

\item{executable}{Is file an executable? (\code{TRUE}/\code{FALSE}) (optional)}

\item{size}{The size of the file (optional)}
}
\value{
Job with references to the files
}
\description{
Use of a logical file name. Used for referencing files in the DAX.
}
\details{
For Use objects that are added to Transformations, the attributes 'link', 'register',
'transfer', 'optional' and 'size' are ignored.

If a File object is passed in as 'file', then the default value for executable
is 'false'. Similarly, if an Executable object is passed in, then the default
value for executable is 'true'.
}
\seealso{
\code{\link{Job}}, \code{\link{Executable}}, \code{\link{File}}
}

