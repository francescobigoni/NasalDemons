####################################################################################################
# CMakeLists file for Nasal Demons
# Generated by Gianluca Elia
# 2020-07-09
####################################################################################################

####################################################################################################
# basic project config
cmake_minimum_required(VERSION 3.5)
set(project_name "Nasal Demons")
set(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake_modules ${CMAKE_MODULE_PATH})
set(CMAKE_CXX_STANDARD 11)

####################################################################################################
# load modules
include(SuperColliderServerPlugin RESULT_VARIABLE server_plugin_found)
if(NOT server_plugin_found)
    message(FATAL_ERROR "Could not find server plugin functions module")
endif()

include(SuperColliderCompilerConfig RESULT_VARIABLE compiler_config_found)
if(NOT compiler_config_found)
    message(FATAL_ERROR "Could not find compiler config module")
endif()

# Windows - puts redistributable DLLs in install directory
include(InstallRequiredSystemLibraries)

sc_check_sc_path("${SC_PATH}")
message(STATUS "Found SuperCollider: ${SC_PATH}")
set(SC_PATH "${SC_PATH}" CACHE PATH
    "Path to SuperCollider source. Relative paths are treated as relative to this script" FORCE)

include("${SC_PATH}/SCVersion.txt")
message(STATUS "Building plugins for SuperCollider version: ${SC_VERSION}")

# set project here to avoid SCVersion.txt clobbering our version info
project(${project_name})
sc_do_initial_compiler_config() # do after setting project so compiler ID is available

if(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT OR NOT CMAKE_INSTALL_PREFIX)
    message(WARNING "No install prefix provided, defaulting to $BUILD_DIR/install")
    set(CMAKE_INSTALL_PREFIX "${CMAKE_BINARY_DIR}/install" CACHE PATH "Install prefix" FORCE)
endif()

message(STATUS "Install directory set to: ${CMAKE_INSTALL_PREFIX}")

####################################################################################################
# options
option(SUPERNOVA "Build plugins for supernova" ON)
option(SCSYNTH "Build plugins for scsynth" ON)
option(NATIVE "Optimize for native architecture" OFF)
option(STRICT "Use strict warning flags" OFF)
option(NOVA_SIMD "Build plugins with nova-simd support." ON)

####################################################################################################
# include libraries

if (NOVA_SIMD)
	add_definitions(-DNOVA_SIMD)
	include_directories(${SC_PATH}/external_libraries/nova-simd)
endif()

####################################################################################################
# Begin target NasalDemons

set(NasalDemons_cpp_files
    plugins/NasalDemons/NasalDemons.cpp
    plugins/NasalDemons/NasalDemons.hpp
)
set(NasalDemons_sc_files
    plugins/NasalDemons/NasalDemons.sc
)
set(NasalDemons_schelp_files
    plugins/NasalDemons/NasalDemons.schelp
    plugins/NasalDemons/NasalDemonsMemBlock.schelp
)

sc_add_server_plugin(
    "NasalDemons/NasalDemons" # desination directory
    "NasalDemons" # target name
    "${NasalDemons_cpp_files}"
    "${NasalDemons_sc_files}"
    "${NasalDemons_schelp_files}"
)

# End target NasalDemons
####################################################################################################

####################################################################################################
# END PLUGIN TARGET DEFINITION
####################################################################################################

message(STATUS "Generating plugin targets done")
