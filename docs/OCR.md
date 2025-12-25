# 使用OCR模型部署文档

使用OCR模型：rednote-hilab/dots.ocr

OCR前端demo及后端部署过程如下：

平台：windows 11
1. 安装wsl: `wsl --install` 并设置好用户名和密码

    迁移wsl到其他盘符命令：

    ``` cmd
    wsl --shutdown
   wsl --export <name of ubuntu> path/to/backup/directory/wsl_backup.tar
   wsl --unregister <name of ubuntu>
   wsl --import <new name> <installation path> <backup path>
   ubuntu config --default-user <username>
   ```
   
2. conda 安装
    ``` bash
    sudo apt update
   sudo apt install build-essential python3-dev -y
   
   wget https://mirrors.tuna.tsinghua.edu.cn/anaconda/miniconda/Miniconda3-latest-Linux-x86_64.sh
   bash Miniconda3-latest-Linux-x86_64.sh
   source ~/.bashrc
    ```
   
3. conda 环境搭建
    ``` bash
    conda create -n dots_ocr python=3.12
   conda activate dots_ocr
   
   git clone https://github.com/rednote-hilab/dots.ocr.git
    cd dots.ocr
   pip install -e . --no-build-isolation
    ```
   安装torch的时候，请根据自己显卡的CUDA版本参考官网上正确的命令运行安装
    
    pytorch历史版本链接: `https://pytorch.org/get-started/previous-versions/`

    注意，在安装flash-attn的时候，从flash-attn的github上下载对应的whl进行安装
    
    安装平台wsl/linux:`https://github.com/Dao-AILab/flash-attention/releases`

    在安装了新的包以后运行如下命令以将修改加载到内存里
    ``` bash
   source ~/.bashrc
   ```
   运行如下命令以切换回搭建好的虚拟环境
    ``` bash
   conda activate dots_ocr
   ```

4. CUDA toolkit 安装

    运行如下命令安装CUDA toolkit
    ``` bash
   sudo apt update
    sudo apt install nvidia-cuda-toolkit -y
   ```
   
    将CUDA_HOME环境变量写入配置文件
    ``` bash
    nano ~/.bashrc
   ```
   在该文件的最底下(按 alt+/ 跳到文件的最底部)加入如下几行
    ``` bash
   export CUDA_HOME=/usr/local/cuda
    export PATH=$CUDA_HOME/bin:$PATH
    export LD_LIBRARY_PATH=$CUDA_HOME/lib64:$LD_LIBRARY_PATH
   ```
   ctrl+o写入，回车以后ctrl+x返回，运行如下命令将改动加载入内存
    ```
   source ~/.bashrc
   ```

5. 代码修改(可选)

    如果运行的是demo前端，将parser.py中的model_name默认值改为`rednote-hilab/dots.ocr`

    可在demo/demo_gradio.py中更改端口等配置

6. vllm后端部署

    如果是国内无法使用huggingface, 在配置文件中加入`export VLLM_USE_MODELSCOPE=True`，加入方式参考4. CUDA toolkit安装

    在搭建好的虚拟环境中运行如下命令
    ```
   vllm serve rednote-hilab/dots.ocr --trust-remote-code --async-scheduling --gpu-memory-utilization 0.95
   ```
   其中gpu-memory-utilization根据自己的显存大小决定数值,从0-1代表显存占用百分比
    等待后端出现 Application startup complete.字样代表后端启动成功

7. demo前端启动

    另起一个终端，进入wsl和配置好的conda虚拟环境，进入项目文件夹
    运行如下命令启动demo前端
    ```
   python demo/demo_gradio.py
   ```