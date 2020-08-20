import socket

HOST, PORT = "localhost", 9999


def recvall(sock):
    BSIZE = 4096
    rec_data = b''
    while True:
        part = sock.recv(BSIZE)
        rec_data += part
        if len(part) < BSIZE:
            break
    return rec_data


def format_age(info_copy):
    info_age = ' '
    val = True
    while info_age != '' and val:
        print("Enter Customer Age:")
        info_age = input()
        info_age = info_age.strip()
        if info_age == '':
            val = False
        else:
            try:
                info_age = int(info_age)
                if info_age < 0:
                    raise ValueError
                info_copy += str(info_age)
                val = False
            except ValueError:
                print("Customer age should be a positive number")
                val = True
    return info_copy


# Create a socket (SOCK_STREAM means a TCP socket)
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client:
    # Connect to server and send data
    client.connect((HOST, PORT))
    # Receive data from the server and shut down
    received = ''
    while received != 'Good bye':
        print("Python DB Menu" + "\n"
              + "\n" +
              "1. Find Customer\n" +
              "2. Add Customer\n" +
              "3. Delete Customer\n" +
              "4. Update Customer age\n" +
              "5. Update Customer address\n" +
              "6. Update Customer phone\n" +
              "7. Print Report\n" +
              "8. Exit\n" +
              "\nSelect:")
        choice = input()
        info = ' '
        if choice == '1' or choice == '3':
            print("Enter Customer Name:")
            info = input().strip()
        elif choice == '2':
            while info == ' ':
                print("Enter Customer Name:")
                info = input().strip()
                if info == "":
                    print("Customer name cannot be blank")
                    info = ' '
            info += ","
            info = format_age(info)
            info += ","
            print("Enter Customer Address:")
            info += input().strip()
            info += ","
            print("Enter Customer Phone:")
            info += input().strip()
        elif choice == '4':
            print("Enter Customer Name:")
            info = input().strip()
            info += ","
            info = format_age(info)
        elif choice == '5':
            print("Enter Customer Name:")
            info = input().strip()
            info += ","
            print("Enter Customer Address:")
            info += input().strip()
        elif choice == '6':
            print("Enter Customer Name:")
            info = input().strip()
            info += ","
            print("Enter Customer Phone:")
            info += input().strip()
        elif choice == '7' or choice == '8':
            print("")
        else:
            print("Invalid Input\n")
            continue
        client.sendall(bytes(choice + "|" + info, "utf-8"))
        received = str(recvall(client), "utf-8").strip()
        if choice == '7' or choice == '1':
            split_by_line = received.split("\n")
            if len(split_by_line) > 1:
                for index in range(0, len(split_by_line)):
                    item = split_by_line[index].split(",")
                    print('{0:10} | {1:3}| {2:30}| {3:12}'.format(item[0], item[1], item[2], item[3]))
                    if index == 0:
                        print("{0:55}".format("______________________________________________________________"))
            else:
                print(split_by_line[0])
            print("\n")
        else:
            print(received + "\n")
